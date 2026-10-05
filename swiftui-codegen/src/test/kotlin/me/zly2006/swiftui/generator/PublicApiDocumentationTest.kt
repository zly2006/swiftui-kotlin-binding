@file:OptIn(org.jetbrains.kotlin.K1Deprecation::class, org.jetbrains.kotlin.config.CompilerConfiguration.Internals::class)

package me.zly2006.swiftui.generator

import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

class PublicApiDocumentationTest {
    @Test fun publicRuntimeDeclarationsHaveKdocAndGeneratedApisLinkOfficialDocumentation() {
        val destination = Files.createTempDirectory("swiftui-kdoc").toFile()
        val disposable = Disposer.newDisposable()
        try {
            generate(destination)
            val environment =
                KotlinCoreEnvironment.createForProduction(
                    disposable,
                    CompilerConfiguration(),
                    EnvironmentConfigFiles.JVM_CONFIG_FILES,
                )
            val factory = KtPsiFactory(environment.project)
            val root = File("..").canonicalFile
            val maintained =
                listOf("swiftui-compose", "swiftui-bridge").flatMap { module ->
                    root
                        .resolve("$module/src")
                        .walkTopDown()
                        .filter { it.isFile && it.extension == "kt" && "Test/" !in it.path }
                        .toList()
                }
            val generated = destination.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
            val missing = mutableListOf<String>()
            for (source in maintained + generated) {
                val file = factory.createFile(source.name, source.readText())
                val declarations = file.collectDescendantsOfType<KtDeclaration>()
                for (declaration in declarations) {
                    if (declaration !is KtClassOrObject &&
                        declaration !is KtNamedFunction &&
                        declaration !is KtProperty &&
                        declaration !is KtParameter
                    ) {
                        continue
                    }
                    if (declaration is KtParameter && !declaration.hasValOrVar()) continue
                    val parents = generateSequence(declaration.parent) { it.parent }.filterIsInstance<KtDeclaration>().toList()
                    val name = (declaration as KtNamedDeclaration).name
                    if ((listOf(declaration) + parents.filterNot { it is KtConstructor<*> }).any {
                            it.hasModifier(KtTokens.PRIVATE_KEYWORD) ||
                                it.hasModifier(KtTokens.INTERNAL_KEYWORD)
                        }
                    ) {
                        continue
                    }
                    if (parents.any { it is KtNamedFunction }) continue
                    val documentation = declaration.docComment?.text
                    val coveredProperty =
                        declaration is KtParameter &&
                            parents.filterIsInstance<KtClassOrObject>().firstOrNull()?.docComment?.text?.contains(
                                "@property $name ",
                            ) ==
                            true
                    if (documentation.isNullOrBlank() && !coveredProperty) {
                        missing += "${source.name}: $name has no KDoc"
                    } else if (source in generated && declaration is KtClassOrObject && name?.endsWith("Config") == true) {
                        if (!documentation.orEmpty().contains("https://developer.apple.com/documentation/")) {
                            missing +=
                                "${source.name}: $name has no official reference"
                        }
                    }
                }
            }
            assertTrue(maintained.isNotEmpty() && generated.isNotEmpty())
            assertTrue(missing.isEmpty(), missing.joinToString("\n"))
            val components = destination.resolve("common/NativeComponents.kt").readText()
            assertTrue(!components.contains("fun Modifier.semanticFont"))
            assertTrue(!components.contains("fun Modifier.captionTwo"))
        } finally {
            Disposer.dispose(disposable)
            destination.deleteRecursively()
        }
    }
}
