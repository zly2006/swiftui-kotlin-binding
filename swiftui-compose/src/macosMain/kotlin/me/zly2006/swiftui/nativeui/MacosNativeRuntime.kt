@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package me.zly2006.swiftui.nativeui

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.interpretObjCPointer
import kotlinx.cinterop.useContents
import me.zly2006.swiftui.bridge.requireNativeMainThread
import me.zly2006.swiftui.capi.sui_node_clear
import me.zly2006.swiftui.capi.sui_node_debug_action
import me.zly2006.swiftui.capi.sui_node_debug_boolean
import me.zly2006.swiftui.capi.sui_node_debug_color
import me.zly2006.swiftui.capi.sui_node_debug_double
import me.zly2006.swiftui.capi.sui_node_debug_string
import me.zly2006.swiftui.capi.sui_node_insert
import me.zly2006.swiftui.capi.sui_node_move
import me.zly2006.swiftui.capi.sui_node_release
import me.zly2006.swiftui.capi.sui_node_remove
import me.zly2006.swiftui.capi.sui_path_live
import me.zly2006.swiftui.capi.sui_path_release
import me.zly2006.swiftui.capi.sui_tree_body_evaluations
import me.zly2006.swiftui.capi.sui_tree_host_create
import me.zly2006.swiftui.capi.sui_tree_host_release
import me.zly2006.swiftui.capi.sui_tree_live_hosts
import me.zly2006.swiftui.capi.sui_tree_live_nodes
import me.zly2006.swiftui.capi.sui_tree_property_updates
import platform.AppKit.NSView
import platform.Foundation.NSProcessInfo

// @formatter:off
internal fun checkNativeUiMainThread() = requireNativeMainThread()
internal class NativeCallback(val invoke: (Any) -> Unit)
private class MacosElement(val owner: BaseMacosNativeUiBackend, val kind: String, var pointer: COpaquePointer?, val callback: StableRef<NativeCallback>?) : NativeUiElement

/** Shared macOS ownership, native tree operations, and bounded path reuse. Main-thread only.
 * @param pathCacheLimits Bounds for geometry retained by this backend.
 */
abstract class BaseMacosNativeUiBackend(pathCacheLimits: NativePathCacheLimits = NativePathCacheLimits()) : NativeTreeBackend, AutoCloseable {
    private val paths = BoundedResourceCache<Path, COpaquePointer>(pathCacheLimits.maxEntries, pathCacheLimits.maxCommands, ::buildNativePath, ::sui_path_release)
    internal fun <R> withNativePath(spec: Path, block: (COpaquePointer) -> R): R {
        checkNativeUiMainThread(); check(!closed)
        return paths.use(spec, spec.commands.size, block)
    }
    /** Returns current path-cache counters and retention on the main thread. */
    fun pathCacheStatistics(): NativePathCacheStatistics {
        checkNativeUiMainThread()
        return NativePathCacheStatistics(paths.hits, paths.constructions, paths.evictions, paths.size, paths.weight)
    }
    private val owned = linkedSetOf<MacosElement>()
    /** Total elements created by this backend. */
    var createdCount = 0; private set
    /** Total elements whose Kotlin ownership has been released. */
    var releasedCount = 0; private set
    /** Elements still owned by this backend, independent of SwiftUI's additional references. */
    val liveCount get() = owned.size
    private var closed = false
    /** Retains the pointer returned by [create] as a backend-owned element of [kind]. */
    protected fun own(kind: String, create: () -> COpaquePointer): NativeUiElement {
        checkNativeUiMainThread(); check(!closed)
        return MacosElement(this, kind, create(), null).also { owned += it; createdCount++ }
    }
    internal fun ownCallback(kind: String, callback: NativeCallback, create: (COpaquePointer) -> COpaquePointer): NativeUiElement {
        checkNativeUiMainThread(); check(!closed)
        val context = StableRef.create(callback)
        try { return MacosElement(this, kind, create(context.asCPointer()), context).also { owned += it; createdCount++ } }
        catch (failure: Throwable) { context.dispose(); throw failure }
    }
    /** Validates backend ownership and optional [kind], then returns the live native pointer. */
    protected fun checked(element: NativeUiElement, kind: String? = null): COpaquePointer {
        checkNativeUiMainThread(); check(!closed)
        val e = element as MacosElement
        check(e.owner === this && (kind == null || e.kind == kind)) { "Native component belongs to another backend or API" }
        return checkNotNull(e.pointer) { "Native component is closed" }
    }
    /** Inserts a detached child into the ordered native tree on the main thread. */
    override fun insert(parent: NativeUiElement, child: NativeUiElement, index: Int) = sui_node_insert(checked(parent), checked(child), index)
    /** Detaches a consecutive range of native children without releasing them. */
    override fun remove(parent: NativeUiElement, index: Int, count: Int) = sui_node_remove(checked(parent), index, count)
    /** Reorders siblings using indices relative to the original child list. */
    override fun move(parent: NativeUiElement, from: Int, to: Int, count: Int) = sui_node_move(checked(parent), from, to, count)
    /** Detaches all native children while retaining their independent ownership. */
    override fun clear(parent: NativeUiElement) = sui_node_clear(checked(parent))
    /** Releases a detached element and disables its callback; repeated release is safe. */
    override fun release(element: NativeUiElement) {
        checkNativeUiMainThread()
        val e = element as MacosElement
        check(e.owner === this)
        e.pointer?.let { sui_node_release(it); e.pointer = null; e.callback?.dispose(); owned -= e; releasedCount++ }
    }
    /** Creates an owned SwiftUI host for [root]; close it after detaching it from the window. */
    fun createHost(root: NativeUiElement): NativeRootView = NativeRootView(checkNotNull(sui_tree_host_create(checked(root))))
    /** Lists currently owned elements with the generated binding name [kind] for diagnostics. */
    fun debugElements(kind: String): List<NativeUiElement> { checkNativeUiMainThread(); return owned.filter { it.kind == kind } }
    /** Invokes an element's native action relay for diagnostics; does not simulate a pointer event. */
    fun debugAction(element: NativeUiElement) = sui_node_debug_action(checked(element))
    /** Sends [value] through a native Boolean callback relay on the main thread. */
    fun debugBoolean(element: NativeUiElement, value: Boolean) = sui_node_debug_boolean(checked(element), if (value) 1 else 0)
    /** Sends [value] through a native text or selection callback relay on the main thread. */
    fun debugString(element: NativeUiElement, value: String) = sui_node_debug_string(checked(element), value)
    /** Sends [value] through a native numeric callback relay on the main thread. */
    fun debugDouble(element: NativeUiElement, value: Double) = sui_node_debug_double(checked(element), value)
    /** Sends [color] through a native color callback relay on the main thread. */
    fun debugColor(element: NativeUiElement, color: Color) = sui_node_debug_color(checked(element), color.kind, color.red, color.green, color.blue, color.alpha, color.opacity)
    /** Releases this owner's native resources on the main thread; safe to call more than once. */
    override fun close() {
        if (closed) return
        checkNativeUiMainThread()
        check(owned.isEmpty()) { "Dispose the owning composition before closing its backend" }
        paths.close()
        closed = true
    }
}

// @formatter:on

/** Owned SwiftUI hosting view. Access and release require Apple's main thread. */
class NativeRootView internal constructor(
    private var pointer: COpaquePointer?,
) : AutoCloseable {
    /** AppKit view to install in a window; access fails after this host has been closed. */
    val nsView: NSView get() {
        checkNativeUiMainThread()
        return interpretObjCPointer(checkNotNull(pointer).rawValue)
    }

    /** Releases this owner's native resources on the main thread; safe to call more than once. */
    override fun close() {
        checkNativeUiMainThread()
        pointer?.let {
            sui_tree_host_release(it)
            pointer = null
        }
    }
}

/** Counts live native node objects, including nodes still retained by SwiftUI. */
fun nativeNodeCount(): Int {
    checkNativeUiMainThread()
    return sui_tree_live_nodes()
}

/** Counts live SwiftUI hosting views on the main thread. */
fun nativeHostCount(): Int {
    checkNativeUiMainThread()
    return sui_tree_live_hosts()
}

/** Returns the cumulative number of native configuration changes. */
fun nativePropertyUpdates(): Long {
    checkNativeUiMainThread()
    return sui_tree_property_updates()
}

/** Returns the cumulative number of generated native view-body evaluations. */
fun nativeBodyEvaluations(): Long {
    checkNativeUiMainThread()
    return sui_tree_body_evaluations()
}

/** Counts retained native path boxes, including backend cache entries. */
fun nativePathResourceCount(): Int {
    checkNativeUiMainThread()
    return sui_path_live()
}

internal fun requireNativeMacosVersion(major: Int) {
    val current = NSProcessInfo.processInfo.operatingSystemVersion.useContents { majorVersion.toInt() }
    require(current >= major) { "Native component requires macOS $major or later" }
}
