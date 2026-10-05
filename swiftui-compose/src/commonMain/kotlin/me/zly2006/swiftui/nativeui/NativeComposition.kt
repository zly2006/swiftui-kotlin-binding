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

/** Opaque element owned by a [NativeTreeBackend]; no platform handles are exposed in common UI code. */
interface NativeUiElement

/** Maintains an ordered native tree. Detach children before releasing their Kotlin ownership. */
interface NativeTreeBackend {
    /** Inserts [child] into [parent] at the zero-based [index]. The child must be detached. */
    fun insert(
        parent: NativeUiElement,
        child: NativeUiElement,
        index: Int,
    )

    /** Detaches [count] consecutive children starting at [index]; their owner releases them separately. */
    fun remove(
        parent: NativeUiElement,
        index: Int,
        count: Int,
    )

    /** Moves [count] siblings from [from] to [to], where [to] refers to the list before removal. */
    fun move(
        parent: NativeUiElement,
        from: Int,
        to: Int,
        count: Int,
    )

    /** Detaches all children of [parent]; it does not release their separate ownership. */
    fun clear(parent: NativeUiElement)

    /** Releases an unparented [element] owned by this backend; repeated release is safe. */
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

/** Compose Runtime composition backed by official native nodes.
 * @property backend Backend owning all native elements created by this composition.
 * @param coroutineContext Scheduling context; native Apple backends require the main thread.
 */
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

    /** Root to host in a native window; unavailable after this composition is closed. */
    val rootElement: NativeUiElement get() {
        check(!closed)
        return root.element
    }

    /** Cumulative frame requests made by Compose; an unchanged UI does not continuously request frames. */
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

    /** Installs or replaces the root composable content. Call on the backend's required thread. */
    fun setContent(content: @Composable () -> Unit) {
        check(!closed)
        composition.setContent { CompositionLocalProvider(LocalNativeUiBackend provides backend, content = content) }
    }

    /** Disposes the composition and its owned tree, cancels scheduling, and unregisters snapshot observation. */
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
