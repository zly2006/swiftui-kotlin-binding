package me.zly2006.swiftui.generator

/** Build-time semantic mappings. Runtime calls are generated individual C functions. */
sealed interface ValueType {
    data object Text : ValueType
    data object Number : ValueType
    data object OptionalNumber : ValueType
    data object Boolean : ValueType
    data object Color : ValueType
    data object Size : ValueType
    data class Enumeration(val name: String) : ValueType
}
data class Field(val name: String, val type: ValueType, val default: String? = null)
data class EnumBinding(val name: String, val swiftType: String, val entries: List<Pair<String, String>>)
enum class Children { None, One, Many }
data class Callback(val name: String, val payload: ValueType? = null)
data class ApiRef(val module: String, val owner: String, val member: String? = null)
data class Binding(
    val name: String,
    val fields: List<Field>,
    val children: Children = Children.None,
    val body: String,
    val references: List<ApiRef>,
    val callback: Callback? = null,
    val modifier: String? = null,
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
    EnumBinding("VerticalAlignment", "SwiftUI.VerticalAlignment", listOf("Center" to "center", "Top" to "top", "Bottom" to "bottom", "FirstTextBaseline" to "firstTextBaseline", "LastTextBaseline" to "lastTextBaseline")),
    EnumBinding("HorizontalAlignment", "SwiftUI.HorizontalAlignment", listOf("Center" to "center", "Leading" to "leading", "Trailing" to "trailing")),
    EnumBinding("Alignment", "SwiftUI.Alignment", listOf("Center" to "center", "Top" to "top", "Bottom" to "bottom", "Leading" to "leading", "Trailing" to "trailing", "TopLeading" to "topLeading", "TopTrailing" to "topTrailing", "BottomLeading" to "bottomLeading", "BottomTrailing" to "bottomTrailing")),
    EnumBinding("FontWeight", "SwiftUI.Font.Weight", listOf("Regular" to "regular", "UltraLight" to "ultraLight", "Thin" to "thin", "Light" to "light", "Medium" to "medium", "Semibold" to "semibold", "Bold" to "bold", "Heavy" to "heavy", "Black" to "black")),
    EnumBinding("FontDesign", "SwiftUI.Font.Design", listOf("Default" to "default", "Serif" to "serif", "Rounded" to "rounded", "Monospaced" to "monospaced")),
    EnumBinding("SymbolMode", "SwiftUI.SymbolRenderingMode", listOf("Monochrome" to "monochrome", "Hierarchical" to "hierarchical", "Multicolor" to "multicolor", "Palette" to "palette")),
    EnumBinding("UnitPoint", "SwiftUI.UnitPoint", listOf("Center" to "center", "Top" to "top", "Bottom" to "bottom", "Leading" to "leading", "Trailing" to "trailing", "TopLeading" to "topLeading", "TopTrailing" to "topTrailing", "BottomLeading" to "bottomLeading", "BottomTrailing" to "bottomTrailing")),
    EnumBinding("Material", "SwiftUI.Material", listOf("Regular" to "regular", "UltraThin" to "ultraThin", "Thin" to "thin", "Thick" to "thick", "UltraThick" to "ultraThick")),
)
val namedColors = listOf("Primary", "Secondary", "White", "Black", "Clear", "Red", "Pink", "Orange", "Yellow", "Green", "Mint", "Teal", "Cyan", "Blue", "Indigo", "Purple", "Gray", "AccentColor")

val adapterDefinitions = listOf(
    Binding("Root", emptyList(), Children.Many, "NativeChildren(node: node)", core("Group")),
    Binding("GeometryReader", emptyList(), Children.One, "SwiftUI.GeometryReader { proxy in NativeChildren(node: node).onChange(of: proxy.size, initial: true) { _, size in node.relay.fire(size) } }", core("GeometryReader"), Callback("onSizeChanged", ValueType.Size)),
    Binding("Tab", listOf(f("value", string), f("title", string), f("systemImage", string)), Children.One, "NativeChildren(node: node)", ui("Tab")),
    Binding("TabSection", listOf(f("title", string)), Children.Many, "NativeChildren(node: node)", ui("TabSection")),
    Binding("SidebarTabs", listOf(f("selection", string)), Children.Many, "NativeSidebarTabView(node: node)", ui("TabView"), Callback("onSelectionChanged", string)),
    Binding("NavigationStack", emptyList(), Children.One, "SwiftUI.NavigationStack { NativeChildren(node: node) }", ui("NavigationStack")),
    Binding("ToolbarHidden", emptyList(), Children.One, "NativeChildren(node: node).toolbar(.hidden)", ui("View", "toolbar"), modifier = "toolbarHidden"),
    Binding("SidebarToggleHidden", emptyList(), Children.One, "NativeChildren(node: node).toolbar(removing: .sidebarToggle)", ui("View", "toolbar"), modifier = "sidebarToggleHidden"),
    Binding("WindowToolbarBackgroundHidden", emptyList(), Children.One, "NativeChildren(node: node).toolbarBackgroundVisibility(.hidden, for: .windowToolbar)", ui("View", "toolbarBackgroundVisibility"), modifier = "windowToolbarBackgroundHidden"),
    Binding("WindowBackground", emptyList(), Children.One, "NativeChildren(node: node).containerBackground(for: .window) { Color(nsColor: .windowBackgroundColor) }", ui("View", "containerBackground"), modifier = "windowBackground"),
    Binding("Locale", listOf(f("identifier", string)), Children.One, "NativeChildren(node: node).environment(\\.locale, Foundation.Locale(identifier: c.identifier))", core("View", "environment"), modifier = "locale"),
    Binding("Text", listOf(f("text", string)), body = "SwiftUI.Text(verbatim: c.text)", references = core("Text", "init(verbatim:)")),
    Binding("Label", listOf(f("text", string), f("systemImage", string)), body = "SwiftUI.Label(c.text, systemImage: c.systemImage)", references = core("Label")),
    Binding("SystemImage", listOf(f("name", string)), body = "SwiftUI.Image(systemName: c.name)", references = core("Image", "init(systemName:)")),
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
    Binding("LinearGradientPair", listOf(f("first", color), f("last", color), f("start", e("UnitPoint"), "UnitPoint.Top"), f("end", e("UnitPoint"), "UnitPoint.Bottom")), body = "SwiftUI.LinearGradient(colors: [c.first.color, c.last.color], startPoint: nativeUnitPoint(c.start), endPoint: nativeUnitPoint(c.end))", references = core("LinearGradient")),
    Binding("LinearGradient", listOf(f("first", color), f("middle", color), f("last", color), f("start", e("UnitPoint"), "UnitPoint.Top"), f("end", e("UnitPoint"), "UnitPoint.Bottom")), body = "SwiftUI.LinearGradient(colors: [c.first.color, c.middle.color, c.last.color], startPoint: nativeUnitPoint(c.start), endPoint: nativeUnitPoint(c.end))", references = core("LinearGradient")),
    Binding("MaterialSurface", listOf(f("material", e("Material"), "Material.Regular"), f("radius", number, "0.0")), body = "SwiftUI.RoundedRectangle(cornerRadius: c.radius, style: .continuous).fill(nativeMaterial(c.material))", references = core("Material") + core("RoundedRectangle")),
    Binding("Button", listOf(f("plain", bool, "false")), Children.One, "if c.plain {\n    SwiftUI.Button(action: { node.relay.fire(()) }) { NativeChildren(node: node) }.buttonStyle(.plain)\n} else {\n    SwiftUI.Button(action: { node.relay.fire(()) }) { NativeChildren(node: node) }\n}", core("Button"), Callback("onClick")),
    Binding("Toggle", listOf(f("checked", bool)), Children.One, "SwiftUI.Toggle(isOn: Binding(get: { c.checked }, set: { node.relay.fire($0) })) { NativeChildren(node: node) }", ui("Toggle"), Callback("onCheckedChanged", bool)),
    Binding("TextField", listOf(f("text", string), f("prompt", string, "\"\"")), body = "SwiftUI.TextField(c.prompt, text: Binding(get: { c.text }, set: { node.relay.fire($0) }))", references = ui("TextField"), callback = Callback("onTextChanged", string)),
    Binding("Slider", listOf(f("value", number), f("minimum", number, "0.0"), f("maximum", number, "1.0")), body = "SwiftUI.Slider(value: Binding(get: { c.value }, set: { node.relay.fire($0) }), in: c.minimum...c.maximum)", references = ui("Slider"), callback = Callback("onValueChanged", number)),
    Binding("ProgressView", listOf(f("value", number)), body = "SwiftUI.ProgressView(value: c.value)", references = ui("ProgressView")),
    Binding("Padding", listOf(f("top", number, "0.0"), f("leading", number, "0.0"), f("bottom", number, "0.0"), f("trailing", number, "0.0")), Children.One, "NativeChildren(node: node).padding(EdgeInsets(top: c.top, leading: c.leading, bottom: c.bottom, trailing: c.trailing))", core("View", "padding"), modifier = "padding"),
    Binding("Frame", listOf(f("width", optional, "null"), f("height", optional, "null"), f("alignment", e("Alignment"), "Alignment.Center")), Children.One, "NativeChildren(node: node).frame(width: optionalNumber(c.width), height: optionalNumber(c.height), alignment: nativeAlignment(c.alignment))", core("View", "frame"), modifier = "frame"),
    Binding("FlexibleFrame", listOf(f("minWidth", optional, "null"), f("idealWidth", optional, "null"), f("maxWidth", optional, "null"), f("minHeight", optional, "null"), f("idealHeight", optional, "null"), f("maxHeight", optional, "null"), f("alignment", e("Alignment"), "Alignment.Center")), Children.One, "NativeChildren(node: node).frame(minWidth: optionalNumber(c.minWidth), idealWidth: optionalNumber(c.idealWidth), maxWidth: optionalNumber(c.maxWidth), minHeight: optionalNumber(c.minHeight), idealHeight: optionalNumber(c.idealHeight), maxHeight: optionalNumber(c.maxHeight), alignment: nativeAlignment(c.alignment))", core("View", "frame"), modifier = "flexibleFrame"),
    Binding("Font", listOf(f("size", number), f("weight", e("FontWeight"), "FontWeight.Regular"), f("design", e("FontDesign"), "FontDesign.Default")), Children.One, "NativeChildren(node: node).font(.system(size: c.size, weight: nativeFontWeight(c.weight), design: nativeFontDesign(c.design)))", core("View", "font"), modifier = "font"),
    Binding("CaptionTwo", listOf(f("weight", e("FontWeight"), "FontWeight.Regular")), Children.One, "NativeChildren(node: node).font(.caption2.weight(nativeFontWeight(c.weight)))", core("View", "font"), modifier = "captionTwo"),
    Binding("CapsuleBackground", listOf(f("color", color)), Children.One, "NativeChildren(node: node).background(c.color.color, in: .capsule)", core("View", "background"), modifier = "capsuleBackground"),
    Binding("Foreground", listOf(f("color", color)), Children.One, "NativeChildren(node: node).foregroundStyle(c.color.color)", core("View", "foregroundStyle"), modifier = "foreground"),
    Binding("TertiaryForeground", emptyList(), Children.One, "NativeChildren(node: node).foregroundStyle(.tertiary)", core("View", "foregroundStyle"), modifier = "tertiaryForeground"),
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
