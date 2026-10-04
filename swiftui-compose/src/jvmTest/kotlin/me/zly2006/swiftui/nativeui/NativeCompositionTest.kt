@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package me.zly2006.swiftui.nativeui

import java.lang.reflect.Proxy
import androidx.compose.runtime.*
import kotlinx.coroutines.test.*
import kotlin.test.*

private class NativeRecorder {
    class Element(val kind: String, var config: Any, val callback: Any? = null) : NativeUiElement { val children = mutableListOf<Element>(); var releases = 0 }
    val all = mutableListOf<Element>()
    val backend = Proxy.newProxyInstance(NativeUiBackend::class.java.classLoader, arrayOf(NativeUiBackend::class.java)) { _, method, raw ->
        val args = raw.orEmpty()
        val name = method.name
        when {
            name.startsWith("create") -> Element(name.removePrefix("create"), args[0], args.getOrNull(1)).also { all += it }
            name.startsWith("update") -> { (args[0] as Element).config = args[1]; null }
            name == "insert" -> { (args[0] as Element).children.add(args[2] as Int, args[1] as Element); null }
            name == "remove" -> { repeat(args[2] as Int) { (args[0] as Element).children.removeAt(args[1] as Int) }; null }
            name == "move" -> {
                val list = (args[0] as Element).children
                val from = args[1] as Int; val to = args[2] as Int; val count = args[3] as Int
                val moving = list.subList(from, from + count).toList()
                repeat(count) { list.removeAt(from) }; list.addAll(if (to > from) to - count else to, moving); null
            }
            name == "clear" -> { (args[0] as Element).children.clear(); null }
            name == "release" -> { (args[0] as Element).releases++; null }
            name == "toString" -> "NativeRecorder"
            name == "hashCode" -> System.identityHashCode(this)
            name == "equals" -> false
            else -> error("Unrecognized backend method $name")
        }
    } as NativeUiBackend
}

class NativeCompositionTest {
    @Test fun drawingUpdatesPreserveNodesAndHoverUsesLatestCallback() = runTest {
        val recorder = NativeRecorder()
        var color by mutableStateOf(NativeColor.Blue)
        var endpoint by mutableStateOf(20.0)
        var callbackVersion by mutableStateOf(1)
        var observedVersion = 0
        var observedHover = false
        val composition = NativeComposition(recorder.backend, StandardTestDispatcher(testScheduler))
        composition.setContent {
            val version = callbackVersion
            val path = NativePath(listOf(NativePathCommand.Move(0.0, 0.0), NativePathCommand.Line(endpoint, endpoint)))
            Box(modifier = NativeModifier.hover { observedVersion = version; observedHover = it }) {
                MeshGradient(2.0, 2.0, listOf(0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 1.0, 1.0), List(4) { color })
                PathFillGradient(path, listOf(color, NativeColor.Clear))
            }
        }
        runCurrent()
        val created = recorder.all.size
        color = NativeColor.Purple
        endpoint = 40.0
        callbackVersion = 2
        runCurrent()
        assertEquals(created, recorder.all.size)
        val mesh = recorder.all.single { it.kind == "MeshGradient" }.config as MeshGradientConfig
        assertEquals(List(4) { NativeColor.Purple }, mesh.colors)
        val fill = recorder.all.single { it.kind == "PathFillGradient" }.config as PathFillGradientConfig
        assertEquals(NativePathCommand.Line(40.0, 40.0), fill.path.commands.last())
        assertEquals(listOf(NativeColor.Purple, NativeColor.Clear), fill.colors)
        @Suppress("UNCHECKED_CAST")
        val hover = recorder.all.single { it.kind == "Hover" }.callback as (Boolean) -> Unit
        hover(true)
        assertEquals(2, observedVersion)
        assertTrue(observedHover)
        val idleFrames = composition.scheduledFrames
        advanceTimeBy(5000); runCurrent()
        assertEquals(idleFrames, composition.scheduledFrames)
        composition.close(); runCurrent()
        assertTrue(recorder.all.all { it.releases == 1 })
    }
    @Test fun generatedControlsUpdateExistingNodesAndUseLatestCallback() = runTest {
        val recorder = NativeRecorder()
        var count by mutableStateOf(0)
        var callbackVersion by mutableStateOf(1)
        var observed = 0
        val composition = NativeComposition(recorder.backend, StandardTestDispatcher(testScheduler))
        composition.setContent {
            val version = callbackVersion
            Column {
                Text("count=$count")
                Button(onClick = { observed = version; count++ }) { Text("update") }
            }
        }
        runCurrent()
        val created = recorder.all.size
        val text = recorder.all.first { it.kind == "Text" }
        val button = recorder.all.single { it.kind == "Button" }
        callbackVersion = 2
        runCurrent()
        @Suppress("UNCHECKED_CAST") val callback = button.callback as () -> Unit
        callback(); runCurrent()
        assertEquals(2, observed)
        assertEquals(TextConfig("count=1"), text.config)
        assertEquals(created, recorder.all.size)
        val idleFrames = composition.scheduledFrames
        advanceTimeBy(5000); runCurrent()
        assertEquals(idleFrames, composition.scheduledFrames)
        composition.close(); runCurrent()
        assertTrue(recorder.all.all { it.releases == 1 })
    }
    @Test fun keyedChildrenMoveWithoutRecreationAndRemovedChildrenReleaseOnce() = runTest {
        val recorder = NativeRecorder()
        var ids by mutableStateOf(listOf("a", "b", "c"))
        val composition = NativeComposition(recorder.backend, StandardTestDispatcher(testScheduler))
        composition.setContent { Column { ids.forEach { id -> key(id) { Text(id) } } } }
        runCurrent()
        val column = recorder.all.single { it.kind == "Column" }
        val original = column.children.toList()
        ids = listOf("c", "a", "b"); runCurrent()
        assertEquals(listOf(original[2], original[0], original[1]), column.children)
        ids = listOf("c", "b"); runCurrent()
        assertEquals(1, original[0].releases)
        assertEquals(listOf(original[2], original[1]), column.children)
        composition.close(); runCurrent()
        assertTrue(recorder.all.all { it.releases == 1 })
    }
}
