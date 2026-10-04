import AppKit
import SwiftUI
import Observation

private var nodeCount: Int32 = 0
private var hostCount: Int32 = 0
private var nextNodeID: Int64 = 0
private var propertyUpdates: Int64 = 0
private var bodyEvaluations: Int64 = 0
func requireNativeMainThread() { precondition(Thread.isMainThread, "Native UI requires the main thread") }
func recordNativePropertyUpdate() { propertyUpdates += 1 }
func recordNativeBodyEvaluation() { bodyEvaluations += 1 }
func optionalNumber(_ value: Double?) -> CGFloat? { value.map { CGFloat($0) } }

@Observable final class NativeChildList { var items: [NativeNode] = [] }
class NativeNode: Identifiable {
    let id: Int64
    let children = NativeChildList()
    let childPolicy: Int
    weak var parent: NativeNode?
    var deactivate: (() -> Void)?
    var nativeView: AnyView { preconditionFailure("An official native API adapter is required") }
    init(childPolicy: Int) {
        requireNativeMainThread()
        nextNodeID += 1
        id = nextNodeID
        self.childPolicy = childPolicy
        nodeCount += 1
    }
    deinit { deactivate?(); nodeCount -= 1 }
}
struct NativeChildren: View {
    let node: NativeNode
    var body: some View {
        ForEach(node.children.items) { child in child.nativeView }
    }
}
final class NativeRelay<Value> {
    var active = true
    let callback: (Value) -> Void
    init(_ callback: @escaping (Value) -> Void) { self.callback = callback }
    func fire(_ value: Value) { requireNativeMainThread(); if active { callback(value) } }
}
func retainNativeNode(_ node: NativeNode) -> UnsafeMutableRawPointer { Unmanaged.passRetained(node).toOpaque() }
func checkedNativeNode<T: NativeNode>(_ pointer: UnsafeMutableRawPointer, _ type: T.Type) -> T {
    requireNativeMainThread()
    guard let node = Unmanaged<NativeNode>.fromOpaque(pointer).takeUnretainedValue() as? T else { preconditionFailure("Native API handle type mismatch") }
    return node
}
private func node(_ pointer: UnsafeMutableRawPointer) -> NativeNode { checkedNativeNode(pointer, NativeNode.self) }

@_cdecl("sui_node_insert")
public func insertNativeNode(_ parentPointer: UnsafeMutableRawPointer, _ childPointer: UnsafeMutableRawPointer, _ index: Int32) {
    let parent = node(parentPointer), child = node(childPointer)
    precondition(parent.childPolicy != 0 && child.parent == nil && parent !== child)
    var ancestor: NativeNode? = parent
    while let current = ancestor { precondition(current !== child); ancestor = current.parent }
    precondition(index >= 0 && Int(index) <= parent.children.items.count)
    child.parent = parent
    parent.children.items.insert(child, at: Int(index))
}
@_cdecl("sui_node_remove")
public func removeNativeNodes(_ pointer: UnsafeMutableRawPointer, _ index: Int32, _ count: Int32) {
    let parent = node(pointer)
    let range = Int(index)..<(Int(index) + Int(count))
    precondition(index >= 0 && count >= 0 && range.upperBound <= parent.children.items.count)
    for child in parent.children.items[range] { child.parent = nil }
    parent.children.items.removeSubrange(range)
}
@_cdecl("sui_node_move")
public func moveNativeNodes(_ pointer: UnsafeMutableRawPointer, _ from: Int32, _ to: Int32, _ count: Int32) {
    let parent = node(pointer)
    let range = Int(from)..<(Int(from) + Int(count))
    precondition(from >= 0 && to >= 0 && count >= 0 && range.upperBound <= parent.children.items.count && Int(to) <= parent.children.items.count)
    let moving = Array(parent.children.items[range])
    parent.children.items.removeSubrange(range)
    let target = Int(to) > Int(from) ? Int(to - count) : Int(to)
    precondition(target >= 0 && target <= parent.children.items.count)
    parent.children.items.insert(contentsOf: moving, at: target)
}
@_cdecl("sui_node_clear")
public func clearNativeNode(_ pointer: UnsafeMutableRawPointer) {
    let parent = node(pointer)
    for child in parent.children.items { child.parent = nil }
    parent.children.items.removeAll()
}
@_cdecl("sui_node_release")
public func releaseNativeNode(_ pointer: UnsafeMutableRawPointer) {
    let value = node(pointer)
    precondition(value.parent == nil, "Detach before releasing Kotlin ownership")
    value.deactivate?()
    Unmanaged<NativeNode>.fromOpaque(pointer).release()
}
private final class NativeRootHost: NSHostingView<AnyView> {
    required init(rootView: AnyView) { super.init(rootView: rootView); hostCount += 1 }
    required init?(coder: NSCoder) { fatalError("Native hosts are created by the Kotlin platform adapter") }
    deinit { hostCount -= 1 }
}
@_cdecl("sui_tree_host_create")
public func createNativeTreeHost(_ pointer: UnsafeMutableRawPointer) -> UnsafeMutableRawPointer {
    let root = node(pointer)
    return Unmanaged.passRetained(NativeRootHost(rootView: root.nativeView)).toOpaque()
}
@_cdecl("sui_tree_host_release")
public func releaseNativeTreeHost(_ pointer: UnsafeMutableRawPointer) {
    requireNativeMainThread()
    let host = Unmanaged<NativeRootHost>.fromOpaque(pointer).takeUnretainedValue()
    host.removeFromSuperview()
    host.rootView = AnyView(EmptyView())
    host.layoutSubtreeIfNeeded()
    Unmanaged<NativeRootHost>.fromOpaque(pointer).release()
}
@_cdecl("sui_tree_live_nodes") public func nativeTreeLiveNodes() -> Int32 { requireNativeMainThread(); return nodeCount }
@_cdecl("sui_tree_live_hosts") public func nativeTreeLiveHosts() -> Int32 { requireNativeMainThread(); return hostCount }
@_cdecl("sui_tree_property_updates") public func nativeTreePropertyUpdates() -> Int64 { requireNativeMainThread(); return propertyUpdates }
@_cdecl("sui_tree_body_evaluations") public func nativeTreeBodyEvaluations() -> Int64 { requireNativeMainThread(); return bodyEvaluations }

// Diagnostics exercise the same relays as official controls, without injecting system input.
@_cdecl("sui_node_debug_action") public func nativeDebugAction(_ pointer: UnsafeMutableRawPointer) { checkedNativeNode(pointer, ButtonNode.self).relay.fire(()) }
@_cdecl("sui_node_debug_boolean") public func nativeDebugBoolean(_ pointer: UnsafeMutableRawPointer, _ value: Int32) { checkedNativeNode(pointer, ToggleNode.self).relay.fire(value != 0) }
@_cdecl("sui_node_debug_string") public func nativeDebugString(_ pointer: UnsafeMutableRawPointer, _ value: UnsafePointer<CChar>) { checkedNativeNode(pointer, TextFieldNode.self).relay.fire(String(cString: value)) }
@_cdecl("sui_node_debug_double") public func nativeDebugDouble(_ pointer: UnsafeMutableRawPointer, _ value: Double) { checkedNativeNode(pointer, SliderNode.self).relay.fire(value) }

// Swift's TabContent cannot be erased as View. This adapter only bridges official Tab APIs.
struct NativeSingleTab: TabContent {
    typealias TabValue = String
    let node: TabNode
    var body: some TabContent<String> {
        let c = node.properties.configuration
        Tab(c.title, systemImage: c.systemImage, value: c.value) { NativeChildren(node: node) }
    }
}
struct NativeSidebarTabView: View {
    let node: SidebarTabsNode
    var body: some View {
        TabView(selection: Binding(get: { node.properties.configuration.selection }, set: { node.relay.fire($0) })) {
            ForEach(node.children.items) { child in
                if let section = child as? TabSectionNode {
                    TabSection(section.properties.configuration.title) {
                        ForEach(section.children.items) { tab in NativeSingleTab(node: tab as! TabNode) }
                    }
                } else {
                    NativeSingleTab(node: child as! TabNode)
                }
            }
        }
        .tabViewStyle(.sidebarAdaptable)
    }
}

private final class NativePathBox { var value = SwiftUI.Path() }
private func nativePathBox(_ pointer: UnsafeMutableRawPointer) -> NativePathBox { requireNativeMainThread(); return Unmanaged<NativePathBox>.fromOpaque(pointer).takeUnretainedValue() }
func copiedNativePath(_ pointer: UnsafeMutableRawPointer) -> SwiftUI.Path { nativePathBox(pointer).value }
@_cdecl("sui_path_create") public func pathCreate() -> UnsafeMutableRawPointer { requireNativeMainThread(); return Unmanaged.passRetained(NativePathBox()).toOpaque() }
@_cdecl("sui_path_move") public func pathMove(_ pointer: UnsafeMutableRawPointer, _ x: Double, _ y: Double) { nativePathBox(pointer).value.move(to: CGPoint(x: x, y: y)) }
@_cdecl("sui_path_line") public func pathLine(_ pointer: UnsafeMutableRawPointer, _ x: Double, _ y: Double) { nativePathBox(pointer).value.addLine(to: CGPoint(x: x, y: y)) }
@_cdecl("sui_path_curve") public func pathCurve(_ pointer: UnsafeMutableRawPointer, _ x: Double, _ y: Double, _ c1x: Double, _ c1y: Double, _ c2x: Double, _ c2y: Double) { nativePathBox(pointer).value.addCurve(to: CGPoint(x: x, y: y), control1: CGPoint(x: c1x, y: c1y), control2: CGPoint(x: c2x, y: c2y)) }
@_cdecl("sui_path_arc") public func pathArc(_ pointer: UnsafeMutableRawPointer, _ x: Double, _ y: Double, _ radius: Double, _ start: Double, _ end: Double, _ clockwise: Int32) { nativePathBox(pointer).value.addArc(center: CGPoint(x: x, y: y), radius: radius, startAngle: .degrees(start), endAngle: .degrees(end), clockwise: clockwise != 0) }
@_cdecl("sui_path_close") public func pathClose(_ pointer: UnsafeMutableRawPointer) { nativePathBox(pointer).value.closeSubpath() }
@_cdecl("sui_path_release") public func pathRelease(_ pointer: UnsafeMutableRawPointer) { requireNativeMainThread(); Unmanaged<NativePathBox>.fromOpaque(pointer).release() }
