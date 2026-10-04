package me.zly2006.swiftui.nativeui

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.coroutines.CoroutineContext
import kotlin.time.TimeSource

interface NativeUiElement

interface NativeTreeBackend {
    fun insert(
        parent: NativeUiElement,
        child: NativeUiElement,
        index: Int,
    )

    fun remove(
        parent: NativeUiElement,
        index: Int,
        count: Int,
    )

    fun move(
        parent: NativeUiElement,
        from: Int,
        to: Int,
        count: Int,
    )

    fun clear(parent: NativeUiElement)

    fun release(element: NativeUiElement)
}

internal val LocalNativeUiBackend =
    staticCompositionLocalOf<NativeUiBackend> { error("Use NativeComposition to host native Composable content") }

internal class UiNode(
    val backend: NativeUiBackend,
    val element: NativeUiElement,
) {
    val children = mutableListOf<UiNode>()
    var released = false

    fun dispose() {
        if (released) return
        backend.clear(element)
        children.forEach { it.dispose() }
        children.clear()
        backend.release(element)
        released = true
    }
}

internal class NativeUiApplier(
    root: UiNode,
) : AbstractApplier<UiNode>(root) {
    override fun insertTopDown(
        index: Int,
        instance: UiNode,
    ) = Unit

    override fun insertBottomUp(
        index: Int,
        instance: UiNode,
    ) {
        current.backend.insert(current.element, instance.element, index)
        current.children.add(index, instance)
    }

    override fun remove(
        index: Int,
        count: Int,
    ) {
        current.backend.remove(current.element, index, count)
        repeat(count) { current.children.removeAt(index).dispose() }
    }

    override fun move(
        from: Int,
        to: Int,
        count: Int,
    ) {
        current.backend.move(current.element, from, to, count)
        current.children.move(from, to, count)
    }

    override fun onClear() {
        root.backend.clear(root.element)
        root.children.forEach { it.dispose() }
        root.children.clear()
    }
}

/** Compose Runtime composition backed by official native nodes, with no drawing engine. */
class NativeComposition(
    val backend: NativeUiBackend,
    coroutineContext: CoroutineContext = Dispatchers.Main,
) : AutoCloseable {
    private val scope = CoroutineScope(SupervisorJob() + coroutineContext)
    private val origin = TimeSource.Monotonic.markNow()
    private var closed = false
    private var frameRequests = 0L
    private lateinit var frameClock: BroadcastFrameClock
    private val notifications = Channel<Unit>(Channel.CONFLATED)
    private val root = UiNode(backend, backend.createRoot(RootConfig))
    val rootElement: NativeUiElement get() {
        check(!closed)
        return root.element
    }
    val scheduledFrames: Long get() = frameRequests
    private val recomposer: Recomposer
    private val composition: Composition
    private val observer: ObserverHandle

    init {
        frameClock =
            BroadcastFrameClock {
                frameRequests++
                scope.launch {
                    yield()
                    frameClock.sendFrame(origin.elapsedNow().inWholeNanoseconds)
                }
            }
        recomposer = Recomposer(coroutineContext + frameClock)
        composition = Composition(NativeUiApplier(root), recomposer)
        observer = Snapshot.registerGlobalWriteObserver { notifications.trySend(Unit) }
        scope.launch { for (ignored in notifications) Snapshot.sendApplyNotifications() }
        scope.launch(frameClock) { recomposer.runRecomposeAndApplyChanges() }
    }

    fun setContent(content: @Composable () -> Unit) {
        check(!closed)
        composition.setContent { CompositionLocalProvider(LocalNativeUiBackend provides backend, content = content) }
    }

    override fun close() {
        if (closed) return
        closed = true
        observer.dispose()
        composition.dispose()
        root.dispose()
        recomposer.cancel()
        notifications.close()
        scope.cancel()
    }
}

/** A chain of generated official native modifier calls, applied in SwiftUI modifier order. */
interface NativeModifier {
    @Composable fun wrap(content: @Composable () -> Unit)

    companion object : NativeModifier {
        @Composable override fun wrap(content: @Composable () -> Unit) = content()
    }
}

private class CombinedNativeModifier(
    val previous: NativeModifier,
    val next: @Composable (@Composable () -> Unit) -> Unit,
) : NativeModifier {
    @Composable override fun wrap(content: @Composable () -> Unit) {
        next { previous.wrap(content) }
    }
}

fun NativeModifier.then(block: @Composable (@Composable () -> Unit) -> Unit): NativeModifier = CombinedNativeModifier(this, block)
