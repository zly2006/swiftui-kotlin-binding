package me.zly2006.swiftui.generator

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// @formatter:off
class WhitelistTest {
    @Test fun coverageUsesIndependentComponentsAndRequiresAllAdapters() {
        assertEquals(nativeUiCatalog.size, nativeUiCatalog.map { it.name }.toSet().size)
        assertTrue(nativeUiCatalog.size > componentWhitelist.size)
        val selected = bindings.map { it.name }.toSet() + kotlinCompositionHelpers
        val available = availableComponents(selected)
        assertTrue(available.size >= (nativeUiCatalog.size * 7 + 9) / 10)
        assertEquals(available, availableComponents(selected + setOf("NewModifier", "AnotherTextOverload")))
        assertFalse(availableComponents(selected - "ResourceImage").any { it.name == "Image" })
        assertFalse(availableComponents(selected - "ForEach").any { it.name == "ForEach" })
        assertFalse(available.any { it.partial || it.name == "EditButton" })
        assertTrue(nativeUiCatalog.all { it.documentation.startsWith("https://developer.apple.com/documentation/") })
    }
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
            assertTrue(first.resolve("deferred-capabilities.tsv").readText().contains("OutlineGroup"))
        } finally { first.deleteRecursively(); second.deleteRecursively() }
    }
}
// @formatter:on
