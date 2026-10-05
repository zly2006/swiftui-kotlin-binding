@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package me.zly2006.swiftui.nativeui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import androidx.compose.foundation.background as composeBackground
import androidx.compose.foundation.layout.padding as composePadding
import androidx.compose.foundation.layout.width as composeWidth
import androidx.compose.ui.draw.alpha as composeAlpha
import androidx.compose.ui.graphics.Color as ComposeColor

private class NativeRecorder {
    class Element(
        val kind: String,
        var config: Any,
        val callback: Any? = null,
    ) : NativeUiElement {
        val children = mutableListOf<Element>()
        var releases = 0
    }

    val all = mutableListOf<Element>()
    val backend =
        Proxy.newProxyInstance(NativeUiBackend::class.java.classLoader, arrayOf(NativeUiBackend::class.java)) { _, method, raw ->
            val args = raw.orEmpty()
            val name = method.name
            when {
                name.startsWith("create") -> Element(name.removePrefix("create"), args[0], args.getOrNull(1)).also { all += it }
                name.startsWith("update") -> {
                    (args[0] as Element).config = args[1]
                    null
                }
                name == "insert" -> {
                    (args[0] as Element).children.add(args[2] as Int, args[1] as Element)
                    null
                }
                name == "remove" -> {
                    repeat(args[2] as Int) { (args[0] as Element).children.removeAt(args[1] as Int) }
                    null
                }
                name == "move" -> {
                    val list = (args[0] as Element).children
                    val from = args[1] as Int
                    val to = args[2] as Int
                    val count = args[3] as Int
                    val moving = list.subList(from, from + count).toList()
                    repeat(count) { list.removeAt(from) }
                    list.addAll(if (to > from) to - count else to, moving)
                    null
                }
                name == "clear" -> {
                    (args[0] as Element).children.clear()
                    null
                }
                name == "release" -> {
                    (args[0] as Element).releases++
                    null
                }
                name == "toString" -> "NativeRecorder"
                name == "hashCode" -> System.identityHashCode(this)
                name == "equals" -> false
                else -> error("Unrecognized backend method $name")
            }
        } as NativeUiBackend
}

class NativeCompositionTest {
    @Test fun standardModifierChainsWrapInComposeOrderAndUpdateExistingNodes() =
        runTest {
            val recorder = NativeRecorder()
            var inset by mutableStateOf(8.0)
            var title by mutableStateOf("before")
            val composition = NativeComposition(recorder.backend, StandardTestDispatcher(testScheduler))
            composition.setContent {
                val decoration = Modifier.background(Color.red).then(Modifier.padding(inset)).frame(width = 100.0)
                Text(title, decoration)
                Text("second", decoration)
            }
            runCurrent()
            val root = recorder.all.single { it.kind == "Root" }
            root.children.forEach { background ->
                assertEquals("Background", background.kind)
                val padding = background.children.single()
                assertEquals("Padding", padding.kind)
                assertEquals("Frame", padding.children.single().kind)
                assertEquals(
                    "Text",
                    padding.children
                        .single()
                        .children
                        .single()
                        .kind,
                )
            }
            val created = recorder.all.size
            inset = 12.0
            title = "after"
            runCurrent()
            assertEquals(created, recorder.all.size)
            assertTrue(recorder.all.filter { it.kind == "Padding" }.all { (it.config as PaddingConfig).top == 12.0 })
            composition.close()
            runCurrent()
            assertTrue(recorder.all.all { it.releases == 1 })
        }

    @Test fun composedModifiersHaveIndependentRememberedStatePerUsage() =
        runTest {
            val recorder = NativeRecorder()
            var identities = 0
            var title by mutableStateOf("before")
            val shared =
                Modifier.composed {
                    val identity = remember { ++identities }
                    Modifier.padding(identity.toDouble())
                }
            val composition = NativeComposition(recorder.backend, StandardTestDispatcher(testScheduler))
            composition.setContent {
                Text(title, shared)
                Text(title, shared)
            }
            runCurrent()
            assertEquals(2, identities)
            assertEquals(listOf(1.0, 2.0), recorder.all.filter { it.kind == "Padding" }.map { (it.config as PaddingConfig).top })
            val created = recorder.all.size
            title = "after"
            runCurrent()
            assertEquals(2, identities)
            assertEquals(created, recorder.all.size)
            composition.close()
            runCurrent()
            assertTrue(recorder.all.all { it.releases == 1 })
        }

    @Test fun unsupportedComposeElementsExplainNativeAlternativesAndDoNotCreateChildren() =
        runTest {
            val cases =
                listOf(
                    Modifier.composePadding(8.dp) to "Modifier.padding(all = 12.0)",
                    Modifier.composeBackground(ComposeColor.Red) to "Modifier.background(Color.red)",
                    Modifier.composeWidth(120.dp) to "Modifier.frame(width = 120.0, height = 40.0)",
                    Modifier.composeAlpha(0.5f) to "Modifier.opacity(0.5)",
                    Modifier.semantics { contentDescription = "Description" } to "Modifier.accessibilityLabel",
                    Modifier.composed { Modifier.composePadding(4.dp) } to "Modifier.padding(all = 12.0)",
                )
            cases.forEach { (modifier, alternative) ->
                val recorder = NativeRecorder()
                val composition = NativeComposition(recorder.backend, StandardTestDispatcher(testScheduler))
                val failure =
                    assertFailsWith<UnsupportedComposeModifierException> {
                        composition.setContent { Text("Unsupported", Modifier.foregroundStyle(Color.blue).then(modifier)) }
                    }
                assertNotNull(failure.replacement)
                assertTrue(failure.message.orEmpty().contains(alternative), failure.message)
                assertTrue(failure.message.orEmpty().contains("me.zly2006.swiftui.nativeui"))
                assertEquals(listOf("Root"), recorder.all.map { it.kind })
                composition.close()
                runCurrent()
                assertTrue(recorder.all.all { it.releases == 1 })
            }
        }

    @Test fun unknownModifierElementsFailWithAUsefulGenericExplanation() =
        runTest {
            val recorder = NativeRecorder()
            val unknown = object : Modifier.Element {}
            val composition = NativeComposition(recorder.backend, StandardTestDispatcher(testScheduler))
            val failure =
                assertFailsWith<UnsupportedComposeModifierException> {
                    composition.setContent { Text("Unknown", Modifier.then(unknown)) }
                }
            assertEquals(null, failure.replacement)
            assertTrue(failure.message.orEmpty().contains("Modifier.composed"))
            assertEquals(listOf("Root"), recorder.all.map { it.kind })
            composition.close()
            runCurrent()
            assertTrue(recorder.all.all { it.releases == 1 })
        }

    @Test fun switchingBetweenComposedAndPlainChainsKeepsTheExistingNativeNodes() =
        runTest {
            val recorder = NativeRecorder()
            var composed by mutableStateOf(false)
            val stateful = Modifier.composed { Modifier.padding(12.0) }
            val composition = NativeComposition(recorder.backend, StandardTestDispatcher(testScheduler))
            composition.setContent { Text("Content", if (composed) stateful else Modifier.padding(8.0)) }
            runCurrent()
            val created = recorder.all.size
            composed = true
            runCurrent()
            assertEquals(created, recorder.all.size)
            assertEquals(12.0, (recorder.all.single { it.kind == "Padding" }.config as PaddingConfig).top)
            composed = false
            runCurrent()
            assertEquals(created, recorder.all.size)
            composition.close()
            runCurrent()
            assertTrue(recorder.all.all { it.releases == 1 })
        }

    @Test fun invalidControlInputsFailBeforeNativeCalls() {
        assertFailsWith<IllegalArgumentException> { LazyVGridConfig(0.0, 8.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { LazyHGridConfig(1.5, 8.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { SliderConfig(0.0, 10.0, 1.0) }
        assertFailsWith<IllegalArgumentException> { StepperConfig("Count", 1.0, 0.0, 100.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { GaugeConfig("Progress", Double.NaN, 0.0, 1.0) }
        assertFailsWith<IllegalArgumentException> { DatePickerConfig("Date", Double.POSITIVE_INFINITY, true, true) }
        assertFailsWith<IllegalArgumentException> { DatePickerConfig("Date", 0.0, date = false, time = false) }
        assertFailsWith<IllegalArgumentException> { AsyncImageConfig("https://example.com/image.png", 0.0, false) }
        assertFailsWith<IllegalArgumentException> { TimelineViewConfig(-1.0, false) }
        assertFailsWith<IllegalArgumentException> { ChartConfig(listOf(0.0), emptyList(), ChartMark.line, "x", "y") }
    }

    @Test fun drawingUpdatesPreserveNodesAndHoverUsesLatestCallback() =
        runTest {
            val recorder = NativeRecorder()
            var color by mutableStateOf(Color.blue)
            var endpoint by mutableStateOf(20.0)
            var callbackVersion by mutableStateOf(1)
            var observedVersion = 0
            var observedHover = false
            val composition = NativeComposition(recorder.backend, StandardTestDispatcher(testScheduler))
            composition.setContent {
                val version = callbackVersion
                val path = Path(listOf(Path.Element.Move(0.0, 0.0), Path.Element.Line(endpoint, endpoint)))
                ZStack(
                    modifier =
                        Modifier.hover {
                            observedVersion = version
                            observedHover = it
                        },
                ) {
                    MeshGradient(2.0, 2.0, listOf(0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 1.0, 1.0), List(4) { color })
                    PathFillGradient(path, listOf(color, Color.clear))
                }
            }
            runCurrent()
            val created = recorder.all.size
            color = Color.purple
            endpoint = 40.0
            callbackVersion = 2
            runCurrent()
            assertEquals(created, recorder.all.size)
            val mesh = recorder.all.single { it.kind == "MeshGradient" }.config as MeshGradientConfig
            assertEquals(List(4) { Color.purple }, mesh.colors)
            val fill = recorder.all.single { it.kind == "PathFillGradient" }.config as PathFillGradientConfig
            assertEquals(Path.Element.Line(40.0, 40.0), fill.path.commands.last())
            assertEquals(listOf(Color.purple, Color.clear), fill.colors)
            @Suppress("UNCHECKED_CAST")
            val hover = recorder.all.single { it.kind == "Hover" }.callback as (Boolean) -> Unit
            hover(true)
            assertEquals(2, observedVersion)
            assertTrue(observedHover)
            val idleFrames = composition.scheduledFrames
            advanceTimeBy(5000)
            runCurrent()
            assertEquals(idleFrames, composition.scheduledFrames)
            composition.close()
            runCurrent()
            assertTrue(recorder.all.all { it.releases == 1 })
        }

    @Test fun generatedControlsUpdateExistingNodesAndUseLatestCallback() =
        runTest {
            val recorder = NativeRecorder()
            var count by mutableStateOf(0)
            var callbackVersion by mutableStateOf(1)
            var observed = 0
            val composition = NativeComposition(recorder.backend, StandardTestDispatcher(testScheduler))
            composition.setContent {
                val version = callbackVersion
                VStack {
                    Text("count=$count")
                    Button(onClick = {
                        observed = version
                        count++
                    }) { Text("update") }
                }
            }
            runCurrent()
            val created = recorder.all.size
            val text = recorder.all.first { it.kind == "Text" }
            val button = recorder.all.single { it.kind == "Button" }
            callbackVersion = 2
            runCurrent()
            @Suppress("UNCHECKED_CAST")
            val callback = button.callback as () -> Unit
            callback()
            runCurrent()
            assertEquals(2, observed)
            assertEquals(TextConfig("count=1"), text.config)
            assertEquals(created, recorder.all.size)
            val idleFrames = composition.scheduledFrames
            advanceTimeBy(5000)
            runCurrent()
            assertEquals(idleFrames, composition.scheduledFrames)
            composition.close()
            runCurrent()
            assertTrue(recorder.all.all { it.releases == 1 })
        }

    @Test fun keyedChildrenMoveWithoutRecreationAndRemovedChildrenReleaseOnce() =
        runTest {
            val recorder = NativeRecorder()
            var ids by mutableStateOf(listOf("a", "b", "c"))
            val composition = NativeComposition(recorder.backend, StandardTestDispatcher(testScheduler))
            composition.setContent { VStack { ForEach(ids, { it }) { Text(it) } } }
            runCurrent()
            val column = recorder.all.single { it.kind == "VStack" }
            val original = column.children.toList()
            ids = listOf("c", "a", "b")
            runCurrent()
            assertEquals(listOf(original[2], original[0], original[1]), column.children)
            ids = listOf("c", "b")
            runCurrent()
            assertEquals(1, original[0].releases)
            assertEquals(listOf(original[2], original[1]), column.children)
            composition.close()
            runCurrent()
            assertTrue(recorder.all.all { it.releases == 1 })
        }
}
