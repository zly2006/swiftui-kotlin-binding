@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
package me.zly2006.swiftui.nativeui

import kotlinx.cinterop.*
import me.zly2006.swiftui.capi.*
import platform.AppKit.NSView
import platform.Foundation.NSThread

internal fun checkNativeUiMainThread() = check(NSThread.isMainThread) { "Native UI requires the main thread" }
internal class NativeCallback(val invoke: (Any) -> Unit)
private class MacosElement(val owner: BaseMacosNativeUiBackend, val kind: String, var pointer: COpaquePointer?, val callback: StableRef<NativeCallback>?) : NativeUiElement

abstract class BaseMacosNativeUiBackend : NativeTreeBackend, AutoCloseable {
    private val owned = linkedSetOf<MacosElement>()
    var createdCount = 0; private set
    var releasedCount = 0; private set
    val liveCount get() = owned.size
    private var closed = false
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
    protected fun checked(element: NativeUiElement, kind: String? = null): COpaquePointer {
        checkNativeUiMainThread(); check(!closed)
        val e = element as MacosElement
        check(e.owner === this && (kind == null || e.kind == kind)) { "Native component belongs to another backend or API" }
        return checkNotNull(e.pointer) { "Native component is closed" }
    }
    override fun insert(parent: NativeUiElement, child: NativeUiElement, index: Int) = sui_node_insert(checked(parent), checked(child), index)
    override fun remove(parent: NativeUiElement, index: Int, count: Int) = sui_node_remove(checked(parent), index, count)
    override fun move(parent: NativeUiElement, from: Int, to: Int, count: Int) = sui_node_move(checked(parent), from, to, count)
    override fun clear(parent: NativeUiElement) = sui_node_clear(checked(parent))
    override fun release(element: NativeUiElement) {
        checkNativeUiMainThread()
        val e = element as MacosElement
        check(e.owner === this)
        e.pointer?.let { sui_node_release(it); e.pointer = null; e.callback?.dispose(); owned -= e; releasedCount++ }
    }
    fun createHost(root: NativeUiElement): NativeRootView = NativeRootView(checkNotNull(sui_tree_host_create(checked(root))))
    fun debugElements(kind: String): List<NativeUiElement> { checkNativeUiMainThread(); return owned.filter { it.kind == kind } }
    fun debugAction(element: NativeUiElement) = sui_node_debug_action(checked(element, "Button"))
    fun debugBoolean(element: NativeUiElement, value: Boolean) = sui_node_debug_boolean(checked(element, "Toggle"), if (value) 1 else 0)
    fun debugString(element: NativeUiElement, value: String) = sui_node_debug_string(checked(element, "TextField"), value)
    fun debugDouble(element: NativeUiElement, value: Double) = sui_node_debug_double(checked(element, "Slider"), value)
    override fun close() {
        if (closed) return
        checkNativeUiMainThread()
        check(owned.isEmpty()) { "Dispose the owning composition before closing its backend" }
        closed = true
    }
}
class NativeRootView internal constructor(private var pointer: COpaquePointer?) : AutoCloseable {
    val nsView: NSView get() { checkNativeUiMainThread(); return interpretObjCPointer(checkNotNull(pointer).rawValue) }
    override fun close() { checkNativeUiMainThread(); pointer?.let { sui_tree_host_release(it); pointer = null } }
}
fun nativeNodeCount(): Int { checkNativeUiMainThread(); return sui_tree_live_nodes() }
fun nativeHostCount(): Int { checkNativeUiMainThread(); return sui_tree_live_hosts() }
fun nativePropertyUpdates(): Long { checkNativeUiMainThread(); return sui_tree_property_updates() }
fun nativeBodyEvaluations(): Long { checkNativeUiMainThread(); return sui_tree_body_evaluations() }
