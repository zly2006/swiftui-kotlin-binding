import AppKit
import SwiftUI
import Observation
import MapKit

private var nodeCount: Int32 = 0
private var hostCount: Int32 = 0
private var nextNodeID: Int64 = 0
private var propertyUpdates: Int64 = 0
private var bodyEvaluations: Int64 = 0
func requireNativeMainThread() { precondition(Thread.isMainThread, "Native UI requires the main thread") }
func recordNativePropertyUpdate() { propertyUpdates += 1 }
func recordNativeBodyEvaluation() { bodyEvaluations += 1 }
func optionalNumber(_ value: Double?) -> CGFloat? { value.map { CGFloat($0) } }
func nativeColorComponents(_ value: SwiftUI.Color) -> NativeColorValue {
    guard let rgb = NSColor(value).usingColorSpace(.sRGB) else { preconditionFailure("Unable to resolve native color") }
    return NativeColorValue(kind: -1, red: rgb.redComponent, green: rgb.greenComponent, blue: rgb.blueComponent, alpha: rgb.alphaComponent, opacity: 1)
}

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
struct NativeChildSlot: View {
    let node: NativeNode
    let index: Int
    @ViewBuilder var body: some View {
        if node.children.items.indices.contains(index) {
            node.children.items[index].nativeView
        }
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

struct NativeStandardTabView: View {
    let node: TabViewNode
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
    }
}

private struct NativeTableValue: Identifiable {
    let node: TableRowNode
    var id: String { node.properties.configuration.identifier }
}
struct NativeTable: View {
    let node: TableNode
    var body: some View {
        let c = node.properties.configuration
        let columns = node.children.items[0].children.items.compactMap { $0 as? TableColumnNode }
        let rows = node.children.items[1].children.items.compactMap { $0 as? TableRowNode }.map { NativeTableValue(node: $0) }
        SwiftUI.Table(rows, selection: Binding<String?>(get: { c.selection.isEmpty ? nil : c.selection }, set: { node.relay.fire($0 ?? "") })) {
            TableColumnForEach(columns) { column in
                TableColumn(column.properties.configuration.title) { (row: NativeTableValue) in
                    NativeTableCell(row: row.node, column: column.properties.configuration.identifier)
                }
            }
        }
    }
}
private struct NativeTableCell: View {
    let row: TableRowNode
    let column: String
    @ViewBuilder var body: some View {
        ForEach(row.children.items) { child in
            if let cell = child as? TableCellNode, cell.properties.configuration.column == column {
                NativeChildren(node: cell)
            }
        }
    }
}

private var nativePathBoxCount: Int32 = 0
private var nativePathConstructionCount: Int64 = 0
private final class NativePathBox {
    var value = SwiftUI.Path()
    init() { nativePathBoxCount += 1; nativePathConstructionCount += 1 }
    deinit { nativePathBoxCount -= 1 }
}
@_cdecl("sui_path_live") public func nativePathLiveCount() -> Int32 { requireNativeMainThread(); return nativePathBoxCount }
@_cdecl("sui_path_constructions") public func nativePathConstructionTotal() -> Int64 { requireNativeMainThread(); return nativePathConstructionCount }
private func nativePathBox(_ pointer: UnsafeMutableRawPointer) -> NativePathBox { requireNativeMainThread(); return Unmanaged<NativePathBox>.fromOpaque(pointer).takeUnretainedValue() }
func copiedNativePath(_ pointer: UnsafeMutableRawPointer) -> SwiftUI.Path { nativePathBox(pointer).value }
@_cdecl("sui_path_create") public func pathCreate() -> UnsafeMutableRawPointer { requireNativeMainThread(); return Unmanaged.passRetained(NativePathBox()).toOpaque() }
@_cdecl("sui_path_move") public func pathMove(_ pointer: UnsafeMutableRawPointer, _ x: Double, _ y: Double) { nativePathBox(pointer).value.move(to: CGPoint(x: x, y: y)) }
@_cdecl("sui_path_line") public func pathLine(_ pointer: UnsafeMutableRawPointer, _ x: Double, _ y: Double) { nativePathBox(pointer).value.addLine(to: CGPoint(x: x, y: y)) }
@_cdecl("sui_path_curve") public func pathCurve(_ pointer: UnsafeMutableRawPointer, _ x: Double, _ y: Double, _ c1x: Double, _ c1y: Double, _ c2x: Double, _ c2y: Double) { nativePathBox(pointer).value.addCurve(to: CGPoint(x: x, y: y), control1: CGPoint(x: c1x, y: c1y), control2: CGPoint(x: c2x, y: c2y)) }
@_cdecl("sui_path_arc") public func pathArc(_ pointer: UnsafeMutableRawPointer, _ x: Double, _ y: Double, _ radius: Double, _ start: Double, _ end: Double, _ clockwise: Int32) { nativePathBox(pointer).value.addArc(center: CGPoint(x: x, y: y), radius: radius, startAngle: .degrees(start), endAngle: .degrees(end), clockwise: clockwise != 0) }
@_cdecl("sui_path_close") public func pathClose(_ pointer: UnsafeMutableRawPointer) { nativePathBox(pointer).value.closeSubpath() }
@_cdecl("sui_path_release") public func pathRelease(_ pointer: UnsafeMutableRawPointer) { requireNativeMainThread(); Unmanaged<NativePathBox>.fromOpaque(pointer).release() }

struct NativeMapRegionValue: Equatable {
    let latitude, longitude, latitudeSpan, longitudeSpan: Double
    init(latitude: Double, longitude: Double, latitudeSpan: Double, longitudeSpan: Double) {
        self.latitude = latitude; self.longitude = longitude; self.latitudeSpan = latitudeSpan; self.longitudeSpan = longitudeSpan
    }
    init(_ value: MKCoordinateRegion) {
        self.init(latitude: value.center.latitude, longitude: value.center.longitude, latitudeSpan: value.span.latitudeDelta, longitudeSpan: value.span.longitudeDelta)
    }
    var region: MKCoordinateRegion { MKCoordinateRegion(center: CLLocationCoordinate2D(latitude: latitude, longitude: longitude), span: MKCoordinateSpan(latitudeDelta: latitudeSpan, longitudeDelta: longitudeSpan)) }
}
