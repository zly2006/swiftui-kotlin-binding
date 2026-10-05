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

private const val APPLE_DOCUMENTATION = "https://developer.apple.com/documentation/swiftui/"

// @formatter:off
val componentWhitelist = listOf(
    WhitelistEntry("Text and images", "Text / Image / Label", setOf("Text", "SystemImage", "Label", "EmptyView", "SecureField", "FileImage", "ResourceImage", "SystemResizableImage", "AsyncImage"), APPLE_DOCUMENTATION + "text-input-and-output", "Reference titles, artwork and symbols", listOf("Attributed Text / selectable text")),
    WhitelistEntry("Stack layout", "HStack / VStack / ZStack / Group", setOf("Row", "Column", "Box", "Group", "Spacer", "Divider", "GeometryReader", "Grid", "GridRow", "LazyVGrid", "LazyHGrid", "ViewThatFits", "HSplitView", "VSplitView"), APPLE_DOCUMENTATION + "layout-fundamentals", "Composition and responsive layouts", emptyList()),
    WhitelistEntry("Scrolling", "ScrollView / LazyVStack / LazyHStack", setOf("ScrollView", "LazyColumn", "LazyRow", "NativeList", "Table", "TableColumn", "TableRow", "TableCell", "Section", "Form", "GroupBox", "ControlGroup", "LabeledContent", "DisclosureGroup", "ScrollViewReader"), APPLE_DOCUMENTATION + "scroll-views", "Reference shelves and long feeds", listOf("scrollPosition", "OutlineGroup / DisclosureTableRow")),
    WhitelistEntry("Controls", "Button / Toggle / TextField / Slider / ProgressView", setOf("Button", "Toggle", "TextField", "TextEditor", "StyledButton", "Slider", "ProgressView", "Picker", "Stepper", "DatePicker", "ColorPicker", "Gauge", "Link", "ShareLink", "Menu", "ContentUnavailableView", "HelpLink", "Map", "VideoPlayer", "Chart", "RenameButton", "TimelineView"), APPLE_DOCUMENTATION + "controls-and-indicators", "Actions and controlled native input", listOf("PasteButton")),
    WhitelistEntry("Navigation", "NavigationStack / TabView / Tab / TabSection", setOf("NavigationStack", "SidebarTabs", "Tab", "TabSection", "ToolbarHidden", "SidebarToggleHidden", "WindowToolbarBackgroundHidden", "WindowBackground", "Locale", "NavigationLink", "NavigationSplitView", "NavigationView", "Id", "TabView", "ToolbarItem", "ToolbarItemGroup", "ToolbarTitleMenu", "ToolbarSpacer"), APPLE_DOCUMENTATION + "navigationstack", "Reference native sidebar and navigation", listOf("sheet / popover / alert / confirmationDialog", "fileImporter / fileExporter")),
    WhitelistEntry("Shapes and materials", "Color / Shape / Gradient / Material", setOf("SolidColor", "Rectangle", "RoundedRectangle", "Circle", "CircleStroke", "GradientCircle", "LinearGradient", "LinearGradientPair", "MeshGradient", "LinearGradientColors", "LinearGradientStops", "RadialGradientColors", "EllipticalGradientColors", "Ellipse", "RoundedGradientBorder", "CircleGradientBorder", "PathStroke", "PathFillGradient", "CanvasStroke", "MaterialSurface", "Capsule", "UnevenRoundedRectangle", "ContainerRelativeShape", "AngularGradientColors", "ConcentricRectangle"), APPLE_DOCUMENTATION + "material", "Native backgrounds, clipping and materials", listOf("ShapeStyle", "native glass effects", "aspectRatio")),
    WhitelistEntry("Layout modifiers", "padding / frame / offset", setOf("Padding", "DefaultPadding", "Frame", "FlexibleFrame", "Offset", "FixedVertical", "IgnoreTopSafeArea"), APPLE_DOCUMENTATION + "view-layout", "UI-owned geometry with official layout semantics", listOf("fixedSize / layoutPriority", "overlay / background content", "alignmentGuide / contentShape")),
    WhitelistEntry("Visual modifiers", "font / foregroundStyle / tint / clipping", setOf("Font", "SemanticFont", "Weight", "RoundedTextField", "RoundedBackground", "RoundedBorder", "StrikeThrough", "Rotation", "Blur", "Tracking", "MultilineCenter", "ClipCircle", "ClipPath", "RoundedMask", "Hover", "ControlSize", "CapsuleBorder", "InsetRoundedBorder", "CircleBorder", "Tag", "PickerStyle", "CaptionTwo", "CapsuleBackground", "Foreground", "TertiaryForeground", "Background", "Tint", "Opacity", "ClipRounded", "Clipped", "Shadow", "Scale", "LineLimit", "SymbolRendering"), APPLE_DOCUMENTATION + "view", "Typography, symbols and native visual composition", listOf("animation / transition / withAnimation", "symbol effects", "environment / preferredColorScheme")),
    WhitelistEntry("Interaction and accessibility", "disabled / accessibilityLabel", setOf("Disabled", "AccessibilityLabel"), APPLE_DOCUMENTATION + "view", "Controlled interactions and accessible native nodes", listOf("focus / submit / search", "contextMenu / keyboardShortcut", "hover / tap / drag / gestures", "accessibility value / hint / actions", "onAppear / onDisappear")),
)
// @formatter:on

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

// @formatter:off
fun printWhitelist() {
    for (entry in componentWhitelist) println("${entry.family}: ${entry.officialApi}\n  generated candidates: ${entry.adapters.sorted().joinToString()}\n  next: ${entry.next.joinToString()}\n  reference: ${entry.documentation}")
    println("Selected ${bindings.size} adapters, including the internal root. Selection and generation do not imply behavior or pixel verification.")
}
// @formatter:on
