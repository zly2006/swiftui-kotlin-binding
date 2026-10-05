package me.zly2006.swiftui.generator

// @formatter:off

/** Build-time semantic mappings. Runtime calls are generated individual C functions. */
sealed interface ValueType {
    data object Text : ValueType
    data object Number : ValueType
    data object OptionalNumber : ValueType
    data object Boolean : ValueType
    data object Color : ValueType
    data object Size : ValueType
    data object Region : ValueType
    data object Numbers : ValueType
    data object Colors : ValueType
    data object Path : ValueType
    data class Enumeration(val name: String) : ValueType
}
data class Field(val name: String, val type: ValueType, val default: String? = null)
data class EnumBinding(val name: String, val swiftType: String, val entries: List<Pair<String, String>>, val nativeMapping: Boolean = true)
enum class Children { None, One, Many }
data class Callback(val name: String, val payload: ValueType? = null)
data class ApiRef(val module: String, val owner: String, val member: String? = null)
data class NativeStorage(val name: String, val type: String, val initializer: String, val dispose: String? = null)
data class Binding(
    val name: String,
    val fields: List<Field>,
    val children: Children = Children.None,
    val body: String,
    val references: List<ApiRef>,
    val callback: Callback? = null,
    val modifier: String? = null,
    val slots: List<String> = emptyList(),
    val storage: List<NativeStorage> = emptyList(),
    val macosMajor: Int? = null,
    val validation: List<String> = emptyList(),
    val handwrittenModifier: Boolean = false,
    val publicComponent: Boolean = true,
) {
    val abi get() = name.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()
}
private fun f(name: String, type: ValueType, default: String? = null) = Field(name, type, default)
private val number = ValueType.Number
private val optional = ValueType.OptionalNumber
private val color = ValueType.Color
private val bool = ValueType.Boolean
private val string = ValueType.Text
private fun e(name: String) = ValueType.Enumeration(name)
private fun core(owner: String, member: String? = null) = listOf(ApiRef("SwiftUICore", owner, member))
private fun ui(owner: String, member: String? = null) = listOf(ApiRef("SwiftUI", owner, member))

val enums = listOf(
    EnumBinding("NativeTextAlignment", "SwiftUI.TextAlignment", listOf("Leading" to "leading", "Center" to "center", "Trailing" to "trailing")),
    EnumBinding("NativeSafeAreaEdges", "SwiftUI.Edge.Set", listOf("All" to "all", "Top" to "top", "Bottom" to "bottom", "Leading" to "leading", "Trailing" to "trailing")),
    EnumBinding("VerticalAlignment", "SwiftUI.VerticalAlignment", listOf("Center" to "center", "Top" to "top", "Bottom" to "bottom", "FirstTextBaseline" to "firstTextBaseline", "LastTextBaseline" to "lastTextBaseline")),
    EnumBinding("HorizontalAlignment", "SwiftUI.HorizontalAlignment", listOf("Center" to "center", "Leading" to "leading", "Trailing" to "trailing")),
    EnumBinding("Alignment", "SwiftUI.Alignment", listOf("Center" to "center", "Top" to "top", "Bottom" to "bottom", "Leading" to "leading", "Trailing" to "trailing", "TopLeading" to "topLeading", "TopTrailing" to "topTrailing", "BottomLeading" to "bottomLeading", "BottomTrailing" to "bottomTrailing")),
    EnumBinding("FontWeight", "SwiftUI.Font.Weight", listOf("Regular" to "regular", "UltraLight" to "ultraLight", "Thin" to "thin", "Light" to "light", "Medium" to "medium", "Semibold" to "semibold", "Bold" to "bold", "Heavy" to "heavy", "Black" to "black")),
    EnumBinding("FontDesign", "SwiftUI.Font.Design", listOf("Default" to "default", "Serif" to "serif", "Rounded" to "rounded", "Monospaced" to "monospaced")),
    EnumBinding("SymbolMode", "SwiftUI.SymbolRenderingMode", listOf("Monochrome" to "monochrome", "Hierarchical" to "hierarchical", "Multicolor" to "multicolor", "Palette" to "palette")),
    EnumBinding("UnitPoint", "SwiftUI.UnitPoint", listOf("Center" to "center", "Top" to "top", "Bottom" to "bottom", "Leading" to "leading", "Trailing" to "trailing", "TopLeading" to "topLeading", "TopTrailing" to "topTrailing", "BottomLeading" to "bottomLeading", "BottomTrailing" to "bottomTrailing")),
    EnumBinding("Material", "SwiftUI.Material", listOf("Regular" to "regular", "UltraThin" to "ultraThin", "Thin" to "thin", "Thick" to "thick", "UltraThick" to "ultraThick")),
    EnumBinding("TextStyle", "SwiftUI.Font", listOf("Body" to "body", "Headline" to "headline", "Subheadline" to "subheadline", "Caption" to "caption", "Title2" to "title2", "LargeTitle" to "largeTitle", "Callout" to "callout", "Footnote" to "footnote", "CaptionTwo" to "caption2")),
    EnumBinding("NativeControlSize", "SwiftUI.ControlSize", listOf("Regular" to "regular", "Mini" to "mini", "Small" to "small", "Large" to "large")),
    EnumBinding("PickerPresentation", "Int32", listOf("Automatic" to "0", "Menu" to "1", "Segmented" to "2", "Inline" to "3", "RadioGroup" to "4"), false),
    EnumBinding("ToolbarPlacement", "SwiftUI.ToolbarItemPlacement", listOf("Automatic" to "automatic", "Principal" to "principal", "Navigation" to "navigation", "PrimaryAction" to "primaryAction", "SecondaryAction" to "secondaryAction", "ConfirmationAction" to "confirmationAction", "CancellationAction" to "cancellationAction", "DestructiveAction" to "destructiveAction", "Status" to "status")),
    EnumBinding("ChartMark", "Int32", listOf("Line" to "0", "Bar" to "1", "Point" to "2", "Area" to "3"), false),
)
val namedColors = listOf("Primary", "Secondary", "White", "Black", "Clear", "Red", "Pink", "Orange", "Yellow", "Green", "Mint", "Teal", "Cyan", "Blue", "Indigo", "Purple", "Gray", "AccentColor", "WindowBackground")

val adapterDefinitions = listOf(
    Binding("MultilineAlignment", listOf(f("alignment",e("NativeTextAlignment"))), Children.One, "NativeChildren(node: node).multilineTextAlignment(nativeNativeTextAlignment(c.alignment))", core("View","multilineTextAlignment"), modifier="multilineAlignment"),
    Binding("FixedSize", listOf(f("horizontal",bool,"true"),f("vertical",bool,"true")), Children.One, "NativeChildren(node: node).fixedSize(horizontal: c.horizontal, vertical: c.vertical)", core("View","fixedSize"), modifier="fixedSize"),
    Binding("IgnoreSafeArea", listOf(f("edges",e("NativeSafeAreaEdges"),"NativeSafeAreaEdges.All")), Children.One, "NativeChildren(node: node).ignoresSafeArea(edges: nativeNativeSafeAreaEdges(c.edges))", core("View","ignoresSafeArea"), modifier="ignoreSafeArea"),
    Binding("BackgroundContent", listOf(f("alignment",e("Alignment"),"Alignment.Center")), Children.Many, "NativeChildSlot(node: node, index: 0).background(alignment: nativeAlignment(c.alignment)) { NativeChildSlot(node: node, index: 1) }", core("View","background"), slots=listOf("content","background")),
    Binding("OverlayContent", listOf(f("alignment",e("Alignment"),"Alignment.Center")), Children.Many, "NativeChildSlot(node: node, index: 0).overlay(alignment: nativeAlignment(c.alignment)) { NativeChildSlot(node: node, index: 1) }", core("View","overlay"), slots=listOf("content","overlay")),
    Binding("MaskContent", listOf(f("alignment",e("Alignment"),"Alignment.Center")), Children.Many, "NativeChildSlot(node: node, index: 0).mask(alignment: nativeAlignment(c.alignment)) { NativeChildSlot(node: node, index: 1) }", core("View","mask"), slots=listOf("content","mask")),
    Binding("RoundedRectangleShape", listOf(f("radius",number)), body="SwiftUI.RoundedRectangle(cornerRadius: c.radius, style: .continuous)", references=core("RoundedRectangle")),
    Binding("RoundedRectangleStroke", listOf(f("color",color),f("radius",number),f("lineWidth",number,"1.0"),f("inset",bool,"false")), body="if c.inset { RoundedRectangle(cornerRadius: c.radius, style: .continuous).strokeBorder(c.color.color, lineWidth: c.lineWidth) } else { RoundedRectangle(cornerRadius: c.radius, style: .continuous).stroke(c.color.color, lineWidth: c.lineWidth) }", references=core("RoundedRectangle","stroke")),
    Binding("CircleOutline", listOf(f("color",color),f("lineWidth",number,"1.0")), body="Circle().stroke(c.color.color, lineWidth: c.lineWidth)", references=core("Circle","stroke")),
    Binding("CapsuleOutline", listOf(f("color",color),f("lineWidth",number,"1.0")), body="Capsule().strokeBorder(c.color.color, lineWidth: c.lineWidth)", references=core("Capsule","strokeBorder")),
    Binding("TimelineView", listOf(f("interval",number,"1.0"),f("paused",bool,"false")), Children.One, "SwiftUI.TimelineView(.animation(minimumInterval: c.interval, paused: c.paused)) { context in NativeChildren(node: node).onChange(of: context.date) { _, value in if !c.paused { node.relay.fire(value.timeIntervalSince1970) } } }", ui("TimelineView"), Callback("onTick",number), validation=listOf("interval.isFinite() && interval > 0")),
    Binding("RenameButton", emptyList(), body="SwiftUI.RenameButton().renameAction { node.relay.fire(()) }", references=ui("RenameButton"), callback=Callback("onRename")),
    Binding("Chart", listOf(f("x",ValueType.Numbers),f("y",ValueType.Numbers),f("mark",e("ChartMark"),"ChartMark.Line"),f("xLabel",string,"\"x\""),f("yLabel",string,"\"y\"")), body="Charts.Chart { ForEach(Array(c.x.indices), id: \\.self) { index in switch c.mark {\ncase 0: LineMark(x: .value(c.xLabel, c.x[index]), y: .value(c.yLabel, c.y[index]))\ncase 1: BarMark(x: .value(c.xLabel, c.x[index]), y: .value(c.yLabel, c.y[index]))\ncase 2: PointMark(x: .value(c.xLabel, c.x[index]), y: .value(c.yLabel, c.y[index]))\ncase 3: AreaMark(x: .value(c.xLabel, c.x[index]), y: .value(c.yLabel, c.y[index]))\ndefault: preconditionFailure(\"Invalid chart mark\")\n} } }", references=listOf(ApiRef("Charts","Chart")), validation=listOf("x.size == y.size", "x.all { it.isFinite() } && y.all { it.isFinite() }")),
    Binding("ToolbarSpacer", listOf(f("placement",e("ToolbarPlacement"),"ToolbarPlacement.Automatic"),f("flexible",bool,"true")), Children.One, "NativeChildren(node: node).toolbar { SwiftUI.ToolbarSpacer(c.flexible ? .flexible : .fixed, placement: nativeToolbarPlacement(c.placement)) }", ui("ToolbarSpacer"), modifier="toolbarSpacer", macosMajor=26),
    Binding("ConcentricRectangle", listOf(f("color",color,"NativeColor.Primary")), body="SwiftUI.ConcentricRectangle().fill(c.color.color)", references=core("ConcentricRectangle"), macosMajor=26),
    Binding("Map", listOf(f("region",ValueType.Region)), body="MapKit.Map(coordinateRegion: Binding(get: { c.region.region }, set: { node.relay.fire(NativeMapRegionValue($0)) }))", references=listOf(ApiRef("MapKit","Map")), callback=Callback("onRegionChanged",ValueType.Region)),
    Binding("VideoPlayer", listOf(f("url",string),f("playing",bool,"false"),f("muted",bool,"false")), body="AVKit.VideoPlayer(player: node.player).onChange(of: c.url, initial: true) { _, value in if let url = URL(string: value), (node.player.currentItem?.asset as? AVURLAsset)?.url != url { node.player.replaceCurrentItem(with: AVPlayerItem(url: url)) } }.onChange(of: c.playing, initial: true) { _, value in if value { node.player.play() } else { node.player.pause() } }.onChange(of: c.muted, initial: true) { _, value in node.player.isMuted = value }", references=listOf(ApiRef("AVKit","VideoPlayer")), storage=listOf(NativeStorage("player","AVPlayer","AVPlayer()","player.pause(); player.replaceCurrentItem(with: nil)"))),
    Binding("ToolbarItem", listOf(f("identifier",string),f("placement",e("ToolbarPlacement"),"ToolbarPlacement.Automatic")), Children.Many, "NativeChildSlot(node: node, index: 0).toolbar { SwiftUI.ToolbarItem(id: c.identifier, placement: nativeToolbarPlacement(c.placement)) { NativeChildSlot(node: node, index: 1) } }", ui("ToolbarItem"), slots=listOf("content","item")),
    Binding("ToolbarItemGroup", listOf(f("placement",e("ToolbarPlacement"),"ToolbarPlacement.Automatic")), Children.Many, "NativeChildSlot(node: node, index: 0).toolbar { SwiftUI.ToolbarItemGroup(placement: nativeToolbarPlacement(c.placement)) { NativeChildSlot(node: node, index: 1) } }", ui("ToolbarItemGroup"), slots=listOf("content","items")),
    Binding("ToolbarTitleMenu", emptyList(), Children.Many, "NativeChildSlot(node: node, index: 0).toolbarTitleMenu { NativeChildSlot(node: node, index: 1) }", ui("ToolbarTitleMenu"), slots=listOf("content","items")),
    Binding("HelpLink", listOf(f("url",string)), body="SwiftUI.HelpLink(destination: URL(string: c.url)!)", references=ui("HelpLink")),
    Binding("TabView", listOf(f("selection",string)), Children.Many, "NativeStandardTabView(node: node)", ui("TabView"), Callback("onSelectionChanged",string)),
    Binding("Table", listOf(f("selection",string,"\"\"")), Children.Many, "NativeTable(node: node)", ui("Table"), Callback("onSelectionChanged",string), slots=listOf("columns","rows")),
    Binding("TableColumn", listOf(f("identifier",string),f("title",string)), body="SwiftUI.EmptyView()", references=ui("TableColumn")),
    Binding("TableRow", listOf(f("identifier",string)), Children.Many, "NativeChildren(node: node)", ui("TableRow")),
    Binding("TableCell", listOf(f("column",string)), Children.One, "NativeChildren(node: node)", ui("TableColumn")),
    Binding("NavigationLink", emptyList(), Children.Many, "SwiftUI.NavigationLink { NativeChildSlot(node: node, index: 1) } label: { NativeChildSlot(node: node, index: 0) }", ui("NavigationLink"), slots=listOf("label","destination")),
    Binding("NavigationSplitView", emptyList(), Children.Many, "SwiftUI.NavigationSplitView { NativeChildSlot(node: node, index: 0) } detail: { NativeChildSlot(node: node, index: 1) }", ui("NavigationSplitView"), slots=listOf("sidebar","detail")),
    Binding("NavigationView", emptyList(), Children.Many, "SwiftUI.NavigationView { NativeChildren(node: node) }", ui("NavigationView")),
    Binding("Id", listOf(f("value",string)), Children.One, "NativeChildren(node: node).id(c.value)", core("View","id"), modifier="id"),
    Binding("ScrollViewReader", listOf(f("target",string),f("anchor",e("UnitPoint"),"UnitPoint.Top")), Children.One, "SwiftUI.ScrollViewReader { proxy in NativeChildren(node: node).onChange(of: c.target) { _, value in proxy.scrollTo(value, anchor: nativeUnitPoint(c.anchor)) } }", ui("ScrollViewReader")),
    Binding("ResourceImage", listOf(f("name",string),f("resizable",bool,"false")), body="if c.resizable { SwiftUI.Image(c.name).resizable() } else { SwiftUI.Image(c.name) }", references=core("Image")),
    Binding("FileImage", listOf(f("file",string),f("resizable",bool,"false")), body="if let image = NSImage(contentsOfFile: c.file) { if c.resizable { SwiftUI.Image(nsImage: image).resizable() } else { SwiftUI.Image(nsImage: image) } }", references=core("Image")),
    Binding("AsyncImage", listOf(f("url",string),f("scale",number,"1.0"),f("resizable",bool,"false")), Children.One, "SwiftUI.AsyncImage(url: URL(string: c.url), scale: c.scale) { image in if c.resizable { image.resizable() } else { image } } placeholder: { NativeChildren(node: node) }", ui("AsyncImage"), validation=listOf("scale.isFinite() && scale > 0")),
    Binding("EmptyView", emptyList(), body = "SwiftUI.EmptyView()", references = core("EmptyView")),
    Binding("SecureField", listOf(f("text", string), f("prompt", string, "\"\"")), body = "SwiftUI.SecureField(c.prompt, text: Binding(get: { c.text }, set: { node.relay.fire($0) }))", references = ui("SecureField"), callback = Callback("onTextChanged", string)),
    Binding("NativeList", emptyList(), Children.Many, "SwiftUI.List { NativeChildren(node: node) }", ui("List")),
    Binding("Section", listOf(f("title", string, "\"\""), f("footer", string, "\"\"")), Children.Many, "SwiftUI.Section { NativeChildren(node: node) } header: { Text(c.title) } footer: { Text(c.footer) }", ui("Section")),
    Binding("Form", emptyList(), Children.Many, "SwiftUI.Form { NativeChildren(node: node) }", ui("Form")),
    Binding("GroupBox", listOf(f("title", string, "\"\"")), Children.Many, "SwiftUI.GroupBox(c.title) { NativeChildren(node: node) }", ui("GroupBox")),
    Binding("ControlGroup", emptyList(), Children.Many, "SwiftUI.ControlGroup { NativeChildren(node: node) }", ui("ControlGroup")),
    Binding("LabeledContent", listOf(f("title", string)), Children.One, "SwiftUI.LabeledContent(c.title) { NativeChildren(node: node) }", ui("LabeledContent")),
    Binding("DisclosureGroup", listOf(f("title", string),f("expanded",bool)), Children.Many, "SwiftUI.DisclosureGroup(c.title, isExpanded: Binding(get: { c.expanded }, set: { node.relay.fire($0) })) { NativeChildren(node: node) }", ui("DisclosureGroup"), Callback("onExpandedChanged",bool)),
    Binding("HSplitView", emptyList(), Children.Many, "SwiftUI.HSplitView { NativeChildren(node: node) }", ui("HSplitView")),
    Binding("VSplitView", emptyList(), Children.Many, "SwiftUI.VSplitView { NativeChildren(node: node) }", ui("VSplitView")),
    Binding("Grid", listOf(f("alignment",e("Alignment"),"Alignment.Center"),f("horizontalSpacing",optional,"null"),f("verticalSpacing",optional,"null")), Children.Many, "SwiftUI.Grid(alignment: nativeAlignment(c.alignment), horizontalSpacing: optionalNumber(c.horizontalSpacing), verticalSpacing: optionalNumber(c.verticalSpacing)) { NativeChildren(node: node) }", core("Grid")),
    Binding("GridRow", listOf(f("alignment",e("VerticalAlignment"),"VerticalAlignment.Center")), Children.Many, "SwiftUI.GridRow(alignment: nativeVerticalAlignment(c.alignment)) { NativeChildren(node: node) }", core("GridRow")),
    Binding("LazyVGrid", listOf(f("columns",number),f("spacing",number,"8.0"),f("minimum",number,"0.0")), Children.Many, "SwiftUI.LazyVGrid(columns: Array(repeating: GridItem(.flexible(minimum: c.minimum), spacing: c.spacing), count: Int(c.columns)), spacing: c.spacing) { NativeChildren(node: node) }", ui("LazyVGrid"), validation=listOf("columns.isFinite() && columns >= 1 && columns <= Int.MAX_VALUE && columns % 1.0 == 0.0", "spacing.isFinite() && spacing >= 0 && minimum.isFinite() && minimum >= 0")),
    Binding("LazyHGrid", listOf(f("rows",number),f("spacing",number,"8.0"),f("minimum",number,"0.0")), Children.Many, "SwiftUI.LazyHGrid(rows: Array(repeating: GridItem(.flexible(minimum: c.minimum), spacing: c.spacing), count: Int(c.rows)), spacing: c.spacing) { NativeChildren(node: node) }", ui("LazyHGrid"), validation=listOf("rows.isFinite() && rows >= 1 && rows <= Int.MAX_VALUE && rows % 1.0 == 0.0", "spacing.isFinite() && spacing >= 0 && minimum.isFinite() && minimum >= 0")),
    Binding("ViewThatFits", emptyList(), Children.Many, "SwiftUI.ViewThatFits { NativeChildren(node: node) }", core("ViewThatFits")),
    Binding("Capsule", listOf(f("color",color,"NativeColor.Primary")), body = "SwiftUI.Capsule().fill(c.color.color)", references = core("Capsule")),
    Binding("UnevenRoundedRectangle", listOf(f("topLeading",number),f("bottomLeading",number),f("bottomTrailing",number),f("topTrailing",number),f("color",color,"NativeColor.Primary")), body = "SwiftUI.UnevenRoundedRectangle(topLeadingRadius: c.topLeading, bottomLeadingRadius: c.bottomLeading, bottomTrailingRadius: c.bottomTrailing, topTrailingRadius: c.topTrailing, style: .continuous).fill(c.color.color)", references = core("UnevenRoundedRectangle")),
    Binding("ContainerRelativeShape", listOf(f("color",color,"NativeColor.Primary")), body = "SwiftUI.ContainerRelativeShape().fill(c.color.color)", references = core("ContainerRelativeShape")),
    Binding("AngularGradientColors", listOf(f("colors",ValueType.Colors),f("center",e("UnitPoint"),"UnitPoint.Center"),f("degrees",number,"0.0")), body = "SwiftUI.AngularGradient(colors: c.colors.map { $0.color }, center: nativeUnitPoint(c.center), angle: .degrees(c.degrees))", references = core("AngularGradient")),
    Binding("Menu", listOf(f("title",string)), Children.Many, "SwiftUI.Menu(c.title) { NativeChildren(node: node) }", ui("Menu")),
    Binding("Picker", listOf(f("title",string),f("selection",string)), Children.Many, "SwiftUI.Picker(c.title, selection: Binding(get: { c.selection }, set: { node.relay.fire($0) })) { NativeChildren(node: node) }", ui("Picker"), Callback("onSelectionChanged",string)),
    Binding("Tag", listOf(f("value",string)), Children.One, "NativeChildren(node: node).tag(c.value)", core("View","tag"), modifier="tag"),
    Binding("PickerStyle", listOf(f("style",e("PickerPresentation"),"PickerPresentation.Automatic")), Children.One, "switch c.style {\ncase 0: NativeChildren(node: node).pickerStyle(.automatic)\ncase 1: NativeChildren(node: node).pickerStyle(.menu)\ncase 2: NativeChildren(node: node).pickerStyle(.segmented)\ncase 3: NativeChildren(node: node).pickerStyle(.inline)\ncase 4: NativeChildren(node: node).pickerStyle(.radioGroup)\ndefault: preconditionFailure(\"Invalid picker presentation\")\n}", ui("View","pickerStyle"), modifier="pickerStyle"),
    Binding("Stepper", listOf(f("title",string),f("value",number),f("minimum",number,"0.0"),f("maximum",number,"100.0"),f("step",number,"1.0")), body="SwiftUI.Stepper(c.title, value: Binding(get: { c.value }, set: { node.relay.fire($0) }), in: c.minimum...c.maximum, step: c.step)", references=ui("Stepper"), callback=Callback("onValueChanged",number), validation=listOf("minimum.isFinite() && maximum.isFinite() && minimum < maximum", "value.isFinite()", "step.isFinite() && step > 0")),
    Binding("DatePicker", listOf(f("title",string),f("unixSeconds",number),f("date",bool,"true"),f("time",bool,"true")), body="SwiftUI.DatePicker(c.title, selection: Binding(get: { Date(timeIntervalSince1970: c.unixSeconds) }, set: { node.relay.fire($0.timeIntervalSince1970) }), displayedComponents: c.date && c.time ? [.date, .hourAndMinute] : c.date ? [.date] : [.hourAndMinute])", references=ui("DatePicker"), callback=Callback("onDateChanged",number), validation=listOf("unixSeconds.isFinite()", "date || time")),
    Binding("ColorPicker", listOf(f("title",string),f("color",color),f("supportsOpacity",bool,"true")), body="SwiftUI.ColorPicker(c.title, selection: Binding(get: { c.color.color }, set: { node.relay.fire(nativeColorComponents($0)) }), supportsOpacity: c.supportsOpacity)", references=ui("ColorPicker"), callback=Callback("onColorChanged",color)),
    Binding("Gauge", listOf(f("title",string),f("value",number),f("minimum",number,"0.0"),f("maximum",number,"1.0")), body="SwiftUI.Gauge(value: c.value, in: c.minimum...c.maximum) { Text(c.title) }", references=ui("Gauge"), validation=listOf("minimum.isFinite() && maximum.isFinite() && minimum < maximum", "value.isFinite()")),
    Binding("Link", listOf(f("title",string),f("url",string)), body="SwiftUI.Link(c.title, destination: URL(string: c.url)!)", references=ui("Link")),
    Binding("ShareLink", listOf(f("title",string),f("text",string)), body="SwiftUI.ShareLink(item: c.text) { Text(c.title) }", references=ui("ShareLink")),
    Binding("ContentUnavailableView", listOf(f("title",string),f("systemImage",string),f("description",string,"\"\"")), body="SwiftUI.ContentUnavailableView(c.title, systemImage: c.systemImage, description: Text(c.description))", references=ui("ContentUnavailableView")),
    Binding("Root", emptyList(), Children.Many, "NativeChildren(node: node)", core("Group")),
    Binding("GeometryReader", emptyList(), Children.One, "SwiftUI.GeometryReader { proxy in NativeChildren(node: node).onChange(of: proxy.size, initial: true) { _, size in node.relay.fire(size) } }", core("GeometryReader"), Callback("onSizeChanged", ValueType.Size)),
    Binding("Tab", listOf(f("value", string), f("title", string), f("systemImage", string)), Children.One, "NativeChildren(node: node)", ui("Tab")),
    Binding("TabSection", listOf(f("title", string)), Children.Many, "NativeChildren(node: node)", ui("TabSection")),
    Binding("SidebarTabs", listOf(f("selection", string)), Children.Many, "NativeSidebarTabView(node: node)", ui("TabView"), Callback("onSelectionChanged", string)),
    Binding("NavigationStack", emptyList(), Children.One, "SwiftUI.NavigationStack { NativeChildren(node: node) }", ui("NavigationStack")),
    Binding("ToolbarHidden", emptyList(), Children.One, "NativeChildren(node: node).toolbar(.hidden)", ui("View", "toolbar"), modifier = "toolbarHidden"),
    Binding("SidebarToggleHidden", emptyList(), Children.One, "NativeChildren(node: node).toolbar(removing: .sidebarToggle)", ui("View", "toolbar"), modifier = "sidebarToggleHidden"),
    Binding("WindowToolbarBackgroundHidden", emptyList(), Children.One, "NativeChildren(node: node).toolbarBackgroundVisibility(.hidden, for: .windowToolbar)", ui("View", "toolbarBackgroundVisibility"), modifier = "windowToolbarBackgroundHidden"),
    Binding("WindowBackground", emptyList(), Children.One, "NativeChildren(node: node).containerBackground(for: .window) { Color(nsColor: .windowBackgroundColor) }", ui("View", "containerBackground"), modifier = "windowBackground", handwrittenModifier = true, publicComponent = false),
    Binding("Locale", listOf(f("identifier", string)), Children.One, "NativeChildren(node: node).environment(\\.locale, Foundation.Locale(identifier: c.identifier))", core("View", "environment"), modifier = "locale"),
    Binding("Text", listOf(f("text", string)), body = "SwiftUI.Text(verbatim: c.text)", references = core("Text", "init(verbatim:)")),
    Binding("Label", listOf(f("text", string), f("systemImage", string)), body = "SwiftUI.Label(c.text, systemImage: c.systemImage)", references = core("Label")),
    Binding("SystemImage", listOf(f("name", string), f("resizable", bool)), body = "if c.resizable { SwiftUI.Image(systemName: c.name).resizable() } else { SwiftUI.Image(systemName: c.name) }", references = core("Image", "init(systemName:)"), publicComponent = false),
    Binding("Row", listOf(f("spacing", optional, "null"), f("alignment", e("VerticalAlignment"), "VerticalAlignment.Center")), Children.Many, "SwiftUI.HStack(alignment: nativeVerticalAlignment(c.alignment), spacing: optionalNumber(c.spacing)) { NativeChildren(node: node) }", core("HStack")),
    Binding("Column", listOf(f("spacing", optional, "null"), f("alignment", e("HorizontalAlignment"), "HorizontalAlignment.Center")), Children.Many, "SwiftUI.VStack(alignment: nativeHorizontalAlignment(c.alignment), spacing: optionalNumber(c.spacing)) { NativeChildren(node: node) }", core("VStack")),
    Binding("Box", listOf(f("alignment", e("Alignment"), "Alignment.Center")), Children.Many, "SwiftUI.ZStack(alignment: nativeAlignment(c.alignment)) { NativeChildren(node: node) }", core("ZStack")),
    Binding("Group", emptyList(), Children.Many, "SwiftUI.Group { NativeChildren(node: node) }", core("Group")),
    Binding("LazyRow", listOf(f("spacing", optional, "null"), f("alignment", e("VerticalAlignment"), "VerticalAlignment.Top")), Children.Many, "SwiftUI.LazyHStack(alignment: nativeVerticalAlignment(c.alignment), spacing: optionalNumber(c.spacing)) { NativeChildren(node: node) }", ui("LazyHStack")),
    Binding("LazyColumn", listOf(f("spacing", optional, "null"), f("alignment", e("HorizontalAlignment"), "HorizontalAlignment.Leading")), Children.Many, "SwiftUI.LazyVStack(alignment: nativeHorizontalAlignment(c.alignment), spacing: optionalNumber(c.spacing)) { NativeChildren(node: node) }", ui("LazyVStack")),
    Binding("ScrollView", listOf(f("horizontal", bool, "false"), f("indicators", bool, "true")), Children.One, "SwiftUI.ScrollView(c.horizontal ? .horizontal : .vertical, showsIndicators: c.indicators) { NativeChildren(node: node) }", ui("ScrollView")),
    Binding("Spacer", listOf(f("minimum", optional, "null")), body = "SwiftUI.Spacer(minLength: optionalNumber(c.minimum))", references = core("Spacer")),
    Binding("Divider", emptyList(), body = "SwiftUI.Divider()", references = core("Divider")),
    Binding("SolidColor", listOf(f("color", color)), body = "c.color.color", references = core("Color")),
    Binding("Rectangle", listOf(f("color", color, "NativeColor.Primary")), body = "SwiftUI.Rectangle().fill(c.color.color)", references = core("Rectangle")),
    Binding("RoundedRectangle", listOf(f("radius", number), f("color", color, "NativeColor.Primary")), body = "SwiftUI.RoundedRectangle(cornerRadius: c.radius, style: .continuous).fill(c.color.color)", references = core("RoundedRectangle")),
    Binding("Circle", listOf(f("color", color, "NativeColor.Primary")), body = "SwiftUI.Circle().fill(c.color.color)", references = core("Circle")),
    Binding("GradientCircle", listOf(f("color", color)), body = "SwiftUI.Circle().fill(c.color.color.gradient)", references = core("Circle")),
    Binding("MaterialSurface", listOf(f("material", e("Material"), "Material.Regular"), f("radius", number, "0.0")), body = "SwiftUI.RoundedRectangle(cornerRadius: c.radius, style: .continuous).fill(nativeMaterial(c.material))", references = core("Material") + core("RoundedRectangle")),
    Binding("Button", listOf(f("plain", bool, "false")), Children.One, "if c.plain {\n    SwiftUI.Button(action: { node.relay.fire(()) }) { NativeChildren(node: node) }.buttonStyle(.plain)\n} else {\n    SwiftUI.Button(action: { node.relay.fire(()) }) { NativeChildren(node: node) }\n}", core("Button"), Callback("onClick")),
    Binding("Toggle", listOf(f("checked", bool)), Children.One, "SwiftUI.Toggle(isOn: Binding(get: { c.checked }, set: { node.relay.fire($0) })) { NativeChildren(node: node) }", ui("Toggle"), Callback("onCheckedChanged", bool)),
    Binding("TextField", listOf(f("text", string), f("prompt", string, "\"\"")), body = "SwiftUI.TextField(c.prompt, text: Binding(get: { c.text }, set: { node.relay.fire($0) }))", references = ui("TextField"), callback = Callback("onTextChanged", string)),
    Binding("Slider", listOf(f("value", number), f("minimum", number, "0.0"), f("maximum", number, "1.0")), body = "SwiftUI.Slider(value: Binding(get: { c.value }, set: { node.relay.fire($0) }), in: c.minimum...c.maximum)", references = ui("Slider"), callback = Callback("onValueChanged", number), validation=listOf("minimum.isFinite() && maximum.isFinite() && minimum < maximum", "value.isFinite()")),
    Binding("ProgressView", listOf(f("value", number)), body = "SwiftUI.ProgressView(value: c.value)", references = ui("ProgressView")),
    Binding("TextEditor", listOf(f("text", string)), body = "SwiftUI.TextEditor(text: Binding(get: { c.text }, set: { node.relay.fire($0) }))", references = ui("TextEditor"), callback = Callback("onTextChanged", string)),
    Binding("StyledButton", listOf(f("prominent", bool, "false")), Children.One, "if c.prominent { SwiftUI.Button(action: { node.relay.fire(()) }) { NativeChildren(node: node) }.buttonStyle(.borderedProminent) } else { SwiftUI.Button(action: { node.relay.fire(()) }) { NativeChildren(node: node) }.buttonStyle(.bordered) }", core("Button"), Callback("onClick")),
    Binding("CircleStroke", listOf(f("color", color), f("lineWidth", number), f("start", number, "0.0"), f("end", number, "1.0"), f("roundCap", bool, "false")), body = "SwiftUI.Circle().trim(from: c.start, to: c.end).stroke(c.color.color, style: StrokeStyle(lineWidth: c.lineWidth, lineCap: c.roundCap ? .round : .butt))", references = core("Circle")),
    Binding("DefaultPadding", emptyList(), Children.One, "NativeChildren(node: node).padding()", core("View", "padding"), modifier = "defaultPadding", handwrittenModifier = true, publicComponent = false),
    Binding("SemanticFont", listOf(f("style", e("TextStyle"))), Children.One, "NativeChildren(node: node).font(nativeTextStyle(c.style))", core("View", "font"), modifier = "semanticFont", handwrittenModifier = true, publicComponent = false),
    Binding("Weight", listOf(f("weight", e("FontWeight"))), Children.One, "NativeChildren(node: node).fontWeight(nativeFontWeight(c.weight))", core("View", "fontWeight"), modifier = "weight"),
    Binding("RoundedTextField", emptyList(), Children.One, "NativeChildren(node: node).textFieldStyle(.roundedBorder)", ui("View", "textFieldStyle"), modifier = "roundedTextField"),
    Binding("StrikeThrough", listOf(f("active", bool)), Children.One, "NativeChildren(node: node).strikethrough(c.active)", core("View", "strikethrough"), modifier = "strikeThrough"),
    Binding("Rotation", listOf(f("degrees", number)), Children.One, "NativeChildren(node: node).rotationEffect(.degrees(c.degrees))", core("View", "rotationEffect"), modifier = "rotation"),
    Binding("Blur", listOf(f("radius", number)), Children.One, "NativeChildren(node: node).blur(radius: c.radius)", core("View", "blur"), modifier = "blur"),
    Binding("Tracking", listOf(f("spacing", number)), Children.One, "NativeChildren(node: node).tracking(c.spacing)", core("View", "tracking"), modifier = "tracking"),
    Binding("MeshGradient", listOf(f("columns", number), f("rows", number), f("points", ValueType.Numbers), f("colors", ValueType.Colors)), body = "SwiftUI.MeshGradient(width: Int(c.columns), height: Int(c.rows), points: stride(from: 0, to: c.points.count, by: 2).map { SIMD2(Float(c.points[$0]), Float(c.points[$0 + 1])) }, colors: c.colors.map { $0.color })", references = core("MeshGradient")),
    Binding("LinearGradientColors", listOf(f("colors", ValueType.Colors), f("start", e("UnitPoint"), "UnitPoint.Top"), f("end", e("UnitPoint"), "UnitPoint.Bottom")), body = "SwiftUI.LinearGradient(colors: c.colors.map { $0.color }, startPoint: nativeUnitPoint(c.start), endPoint: nativeUnitPoint(c.end))", references = core("LinearGradient")),
    Binding("LinearGradientStops", listOf(f("colors", ValueType.Colors), f("locations", ValueType.Numbers), f("start", e("UnitPoint"), "UnitPoint.Top"), f("end", e("UnitPoint"), "UnitPoint.Bottom")), body = "SwiftUI.LinearGradient(stops: zip(c.colors, c.locations).map { Gradient.Stop(color: $0.0.color, location: $0.1) }, startPoint: nativeUnitPoint(c.start), endPoint: nativeUnitPoint(c.end))", references = core("LinearGradient")),
    Binding("RadialGradientColors", listOf(f("colors", ValueType.Colors), f("centerX", number), f("centerY", number), f("startRadius", number), f("endRadius", number)), body = "SwiftUI.RadialGradient(colors: c.colors.map { $0.color }, center: SwiftUI.UnitPoint(x: c.centerX, y: c.centerY), startRadius: c.startRadius, endRadius: c.endRadius)", references = core("RadialGradient")),
    Binding("EllipticalGradientColors", listOf(f("colors", ValueType.Colors), f("centerX", number), f("centerY", number), f("startFraction", number), f("endFraction", number)), body = "SwiftUI.EllipticalGradient(colors: c.colors.map { $0.color }, center: SwiftUI.UnitPoint(x: c.centerX, y: c.centerY), startRadiusFraction: c.startFraction, endRadiusFraction: c.endFraction)", references = core("EllipticalGradient")),
    Binding("Ellipse", listOf(f("color", color)), body = "SwiftUI.Ellipse().fill(c.color.color)", references = core("Ellipse")),
    Binding("ClipCircle", emptyList(), Children.One, "NativeChildren(node: node).clipShape(Circle())", core("View", "clipShape"), modifier = "clipCircle"),
    Binding("RoundedGradientBorder", listOf(f("colors", ValueType.Colors), f("start", e("UnitPoint")), f("end", e("UnitPoint")), f("radius", number), f("lineWidth", number)), body = "RoundedRectangle(cornerRadius: c.radius, style: .continuous).strokeBorder(LinearGradient(colors: c.colors.map { $0.color }, startPoint: nativeUnitPoint(c.start), endPoint: nativeUnitPoint(c.end)), lineWidth: c.lineWidth)", references = core("RoundedRectangle")),
    Binding("CircleGradientBorder", listOf(f("colors", ValueType.Colors), f("lineWidth", number)), body = "Circle().strokeBorder(LinearGradient(colors: c.colors.map { $0.color }, startPoint: .top, endPoint: .bottom), lineWidth: c.lineWidth)", references = core("Circle")),
    Binding("ClipPath", listOf(f("path", ValueType.Path)), Children.One, "NativeChildren(node: node).clipShape(c.path)", core("View", "clipShape"), modifier = "clipPath"),
    Binding("Hover", emptyList(), Children.One, "NativeChildren(node: node).onHover { node.relay.fire($0) }", core("View", "onHover"), Callback("onHoverChanged", bool), modifier = "hover"),
    Binding("ControlSize", listOf(f("size",e("NativeControlSize"))), Children.One, "NativeChildren(node: node).controlSize(nativeNativeControlSize(c.size))", ui("View","controlSize"), modifier = "controlSize"),
    Binding("PathFillGradient", listOf(f("path",ValueType.Path),f("colors",ValueType.Colors),f("start",e("UnitPoint"),"UnitPoint.Top"),f("end",e("UnitPoint"),"UnitPoint.Bottom")), body = "c.path.fill(LinearGradient(colors: c.colors.map { $0.color }, startPoint: nativeUnitPoint(c.start), endPoint: nativeUnitPoint(c.end)))", references = core("Path")),
    Binding("CanvasStroke", listOf(f("path",ValueType.Path),f("color",color),f("lineWidth",number)), body = "Canvas { context, _ in context.stroke(c.path, with: .color(c.color.color), lineWidth: c.lineWidth) }", references = core("Canvas")),
    Binding("PathStroke", listOf(f("path", ValueType.Path), f("color", color), f("lineWidth", number)), body = "c.path.stroke(c.color.color, lineWidth: c.lineWidth)", references = core("Path")),
    Binding("Padding", listOf(f("top", number, "0.0"), f("leading", number, "0.0"), f("bottom", number, "0.0"), f("trailing", number, "0.0")), Children.One, "NativeChildren(node: node).padding(EdgeInsets(top: c.top, leading: c.leading, bottom: c.bottom, trailing: c.trailing))", core("View", "padding"), modifier = "padding"),
    Binding("Frame", listOf(f("width", optional, "null"), f("height", optional, "null"), f("alignment", e("Alignment"), "Alignment.Center")), Children.One, "NativeChildren(node: node).frame(width: optionalNumber(c.width), height: optionalNumber(c.height), alignment: nativeAlignment(c.alignment))", core("View", "frame"), modifier = "frame"),
    Binding("FlexibleFrame", listOf(f("minWidth", optional, "null"), f("idealWidth", optional, "null"), f("maxWidth", optional, "null"), f("minHeight", optional, "null"), f("idealHeight", optional, "null"), f("maxHeight", optional, "null"), f("alignment", e("Alignment"), "Alignment.Center")), Children.One, "NativeChildren(node: node).frame(minWidth: optionalNumber(c.minWidth), idealWidth: optionalNumber(c.idealWidth), maxWidth: optionalNumber(c.maxWidth), minHeight: optionalNumber(c.minHeight), idealHeight: optionalNumber(c.idealHeight), maxHeight: optionalNumber(c.maxHeight), alignment: nativeAlignment(c.alignment))", core("View", "frame"), modifier = "flexibleFrame"),
    Binding("Font", listOf(f("size", number), f("weight", e("FontWeight"), "FontWeight.Regular"), f("design", e("FontDesign"), "FontDesign.Default")), Children.One, "NativeChildren(node: node).font(.system(size: c.size, weight: nativeFontWeight(c.weight), design: nativeFontDesign(c.design)))", core("View", "font"), modifier = "font"),
    Binding("Foreground", listOf(f("color", color)), Children.One, "NativeChildren(node: node).foregroundStyle(c.color.color)", core("View", "foregroundStyle"), modifier = "foreground"),
    Binding("TertiaryForeground", emptyList(), Children.One, "NativeChildren(node: node).foregroundStyle(.tertiary)", core("View", "foregroundStyle"), modifier = "tertiaryForeground", handwrittenModifier = true, publicComponent = false),
    Binding("Background", listOf(f("color", color)), Children.One, "NativeChildren(node: node).background(c.color.color)", core("View", "background"), modifier = "background"),
    Binding("Tint", listOf(f("color", color)), Children.One, "NativeChildren(node: node).tint(c.color.color)", ui("View", "tint"), modifier = "tint"),
    Binding("Opacity", listOf(f("value", number)), Children.One, "NativeChildren(node: node).opacity(c.value)", core("View", "opacity"), modifier = "opacity"),
    Binding("Offset", listOf(f("x", number, "0.0"), f("y", number, "0.0")), Children.One, "NativeChildren(node: node).offset(x: c.x, y: c.y)", core("View", "offset"), modifier = "offset"),
    Binding("ClipRounded", listOf(f("radius", number)), Children.One, "NativeChildren(node: node).clipShape(.rect(cornerRadius: c.radius, style: .continuous))", core("View", "clipShape"), modifier = "clipRounded"),
    Binding("Clipped", emptyList(), Children.One, "NativeChildren(node: node).clipped()", core("View", "clipped"), modifier = "clipped"),
    Binding("Shadow", listOf(f("color", color), f("radius", number), f("x", number, "0.0"), f("y", number, "0.0")), Children.One, "NativeChildren(node: node).shadow(color: c.color.color, radius: c.radius, x: c.x, y: c.y)", core("View", "shadow"), modifier = "shadow"),
    Binding("Scale", listOf(f("x", number, "1.0"), f("y", number, "1.0")), Children.One, "NativeChildren(node: node).scaleEffect(x: c.x, y: c.y)", core("View", "scaleEffect"), modifier = "scale"),
    Binding("LineLimit", listOf(f("lines", number)), Children.One, "NativeChildren(node: node).lineLimit(Int(c.lines))", core("View", "lineLimit"), modifier = "lineLimit"),
    Binding("SymbolRendering", listOf(f("mode", e("SymbolMode"))), Children.One, "NativeChildren(node: node).symbolRenderingMode(nativeSymbolMode(c.mode))", core("View", "symbolRenderingMode"), modifier = "symbolRendering"),
    Binding("AccessibilityLabel", listOf(f("text", string)), Children.One, "NativeChildren(node: node).accessibilityLabel(c.text)", ui("View", "accessibilityLabel"), modifier = "accessibilityLabel"),
    Binding("Disabled", listOf(f("disabled", bool)), Children.One, "NativeChildren(node: node).disabled(c.disabled)", core("View", "disabled"), modifier = "disabled"),
)
// @formatter:on
