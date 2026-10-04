package me.zly2006.swiftui.generator

import java.nio.file.Files
import kotlin.test.*

class WhitelistTest {
    @Test fun unlistedDefinitionDoesNotExportAnApi() {
        val privateCandidate = Binding("UnreviewedControl", emptyList(), body = "SwiftUI.EmptyView()", references = emptyList())
        assertFalse(selectedBindings(adapterDefinitions + privateCandidate).any { it.name == privateCandidate.name })
    }
    @Test fun missingSelectedAdapterFailsRatherThanSilentlyDroppingIt() {
        assertFailsWith<IllegalStateException> { selectedBindings(adapterDefinitions.filterNot { it.name == "Text" }) }
    }
    @Test fun generatedFilesAreDeterministicAndContainOnlySelectedExports() {
        val first = Files.createTempDirectory("swiftui-bindings-first").toFile()
        val second = Files.createTempDirectory("swiftui-bindings-second").toFile()
        try {
            generate(first); generate(second)
            val names = first.walkTopDown().filter { it.isFile }.map { it.relativeTo(first).path }.toSet()
            assertEquals(names, second.walkTopDown().filter { it.isFile }.map { it.relativeTo(second).path }.toSet())
            names.forEach { assertContentEquals(first.resolve(it).readBytes(), second.resolve(it).readBytes()) }
            val header = first.resolve("include/NativeUI.h").readText()
            assertEquals(bindings.size, Regex("SUI_Node sui_node_[a-z_]+_create").findAll(header).count())
            assertTrue(first.resolve("deferred-capabilities.tsv").readText().contains("Picker"))
        } finally { first.deleteRecursively(); second.deleteRecursively() }
    }
}
