package me.zly2006.swiftui.generator

/** Curated UI capabilities, not an SDK symbol inventory. Add candidates after source review. */
data class WhitelistEntry(
    val family: String,
    val officialApi: String,
    val adapters: Set<String>,
    val documentation: String,
    val reason: String,
    val next: List<String> = emptyList(),
)
private const val apple = "https://developer.apple.com/documentation/swiftui/"
val componentWhitelist = listOf(
    WhitelistEntry("Text and images", "Text / Image / Label", setOf("Text", "SystemImage", "Label"), apple + "text-input-and-output", "Reference titles, artwork and symbols", listOf("Image resources / AsyncImage", "SecureField", "TextEditor")),
    WhitelistEntry("Stack layout", "HStack / VStack / ZStack / Group", setOf("Row", "Column", "Box", "Group", "Spacer", "Divider", "GeometryReader"), apple + "layout-fundamentals", "Composition and responsive layouts", listOf("Grid / GridRow", "LazyVGrid / LazyHGrid")),
    WhitelistEntry("Scrolling", "ScrollView / LazyVStack / LazyHStack", setOf("ScrollView", "LazyColumn", "LazyRow"), apple + "scroll-views", "Reference shelves and long feeds", listOf("ScrollViewReader / scrollPosition", "List / Section / Form", "Table / OutlineGroup / DisclosureGroup")),
    WhitelistEntry("Controls", "Button / Toggle / TextField / Slider / ProgressView", setOf("Button", "Toggle", "TextField", "TextEditor", "StyledButton", "Slider", "ProgressView"), apple + "controls-and-indicators", "Actions and controlled native input", listOf("Picker", "DatePicker", "ColorPicker", "Stepper", "LabeledContent", "Gauge", "Link / ShareLink", "Menu / ControlGroup")),
    WhitelistEntry("Navigation", "NavigationStack / TabView / Tab / TabSection", setOf("NavigationStack", "SidebarTabs", "Tab", "TabSection", "ToolbarHidden", "SidebarToggleHidden", "WindowToolbarBackgroundHidden", "WindowBackground", "Locale"), apple + "navigationstack", "Reference native sidebar and navigation", listOf("NavigationLink / NavigationSplitView", "toolbar items", "sheet / popover / alert / confirmationDialog", "fileImporter / fileExporter")),
    WhitelistEntry("Shapes and materials", "Color / Shape / Gradient / Material", setOf("SolidColor", "Rectangle", "RoundedRectangle", "Circle", "CircleStroke", "GradientCircle", "LinearGradient", "LinearGradientPair", "MeshGradient", "LinearGradientColors", "LinearGradientStops", "RadialGradientColors", "EllipticalGradientColors", "Ellipse", "RoundedGradientBorder", "CircleGradientBorder", "PathStroke", "PathFillGradient", "CanvasStroke", "MaterialSurface"), apple + "material", "Native backgrounds, clipping and materials", listOf("AngularGradient / RadialGradient", "ShapeStyle", "native glass effects", "Image resizable / aspectRatio")),
    WhitelistEntry("Layout modifiers", "padding / frame / offset", setOf("Padding", "DefaultPadding", "Frame", "FlexibleFrame", "Offset", "FixedVertical", "IgnoreTopSafeArea"), apple + "view-layout", "UI-owned geometry with official layout semantics", listOf("fixedSize / layoutPriority", "overlay / background content", "alignmentGuide / contentShape")),
    WhitelistEntry("Visual modifiers", "font / foregroundStyle / tint / clipping", setOf("Font", "SemanticFont", "Weight", "RoundedTextField", "RoundedBackground", "RoundedBorder", "StrikeThrough", "Rotation", "Blur", "Tracking", "MultilineCenter", "ClipCircle", "ClipPath", "RoundedMask", "Hover", "ControlSize", "CapsuleBorder", "InsetRoundedBorder", "CircleBorder", "CaptionTwo", "CapsuleBackground", "Foreground", "TertiaryForeground", "Background", "Tint", "Opacity", "ClipRounded", "Clipped", "Shadow", "Scale", "LineLimit", "SymbolRendering"), apple + "view", "Typography, symbols and native visual composition", listOf("animation / transition / withAnimation", "symbol effects", "environment / preferredColorScheme")),
    WhitelistEntry("Interaction and accessibility", "disabled / accessibilityLabel", setOf("Disabled", "AccessibilityLabel"), apple + "view", "Controlled interactions and accessible native nodes", listOf("focus / submit / search", "contextMenu / keyboardShortcut", "hover / tap / drag / gestures", "accessibility value / hint / actions", "onAppear / onDisappear")),
)
private val runtimeWhitelist = setOf("Root")

/** Every emitted native adapter must be explicitly selected. Definitions alone cannot export APIs. */
fun selectedBindings(definitions: List<Binding> = adapterDefinitions): List<Binding> {
    val allowed = runtimeWhitelist + componentWhitelist.flatMap { it.adapters }
    val duplicates = definitions.groupBy { it.name }.filterValues { it.size != 1 }.keys
    check(duplicates.isEmpty()) { "Duplicate native adapters: $duplicates" }
    val indexed = definitions.associateBy { it.name }
    check(allowed.all { it in indexed }) { "Whitelist lacks semantic adapter definitions: ${allowed - indexed.keys}" }
    return definitions.filter { it.name in allowed }
}
val bindings: List<Binding> get() = selectedBindings()
fun printWhitelist() {
    for (entry in componentWhitelist) println("${entry.family}: ${entry.officialApi}\n  generated candidates: ${entry.adapters.sorted().joinToString()}\n  next: ${entry.next.joinToString()}\n  reference: ${entry.documentation}")
    println("Selected ${bindings.size} adapters, including the internal root. Selection and generation do not imply behavior or pixel verification.")
}
