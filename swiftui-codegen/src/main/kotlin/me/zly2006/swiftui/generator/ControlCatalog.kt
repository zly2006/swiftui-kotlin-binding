package me.zly2006.swiftui.generator

/** Pinned source snapshots for approximate component usage counts. */
data class ControlUsageSource(
    val repository: String,
    val commit: String,
    val swiftFiles: Int,
)

val controlUsageSources =
    listOf(
        ControlUsageSource("CodeEditApp/CodeEdit", "fa2aebd86373211c78626074b53ab75010767575", 628),
        ControlUsageSource("Dimillian/IceCubesApp", "9efcb16e720f337a401cf61c8e300dd043368282", 401),
        ControlUsageSource("insidegui/VirtualBuddy", "90a7b0c0df8068c39546d47baf36fff9f0797b1e", 270),
    )

/** Independent official UI inventory; modifiers, overloads and generated wrappers do not count. */
data class UiComponent(
    val name: String,
    val family: String,
    val documentation: String,
    val macosSince: String?,
    val sourceFiles: Int,
    val adapters: Set<String>,
    val partial: Boolean = false,
)

// @formatter:off
val nativeUiCatalog = listOf(
    UiComponent("Text", "text-input-and-output", "https://developer.apple.com/documentation/swiftui/text", "10.15", 281, setOf("Text"), false),
    UiComponent("Button", "controls-and-indicators", "https://developer.apple.com/documentation/swiftui/button", "10.15", 233, setOf("Button"), false),
    UiComponent("HStack", "layout-fundamentals", "https://developer.apple.com/documentation/swiftui/hstack", "10.15", 205, setOf("Row"), false),
    UiComponent("VStack", "layout-fundamentals", "https://developer.apple.com/documentation/swiftui/vstack", "10.15", 183, setOf("Column"), false),
    UiComponent("Image", "images", "https://developer.apple.com/documentation/swiftui/image", "10.15", 174, setOf("SystemImage", "FileImage", "ResourceImage"), false),
    UiComponent("ForEach", "view-groupings", "https://developer.apple.com/documentation/swiftui/foreach", "10.15", 152, setOf("ForEach"), false),
    UiComponent("Spacer", "layout-fundamentals", "https://developer.apple.com/documentation/swiftui/spacer", "10.15", 140, setOf("Spacer"), false),
    UiComponent("Section", "view-groupings", "https://developer.apple.com/documentation/swiftui/section", "10.15", 87, setOf("Section"), false),
    UiComponent("Color", "drawing-and-graphics", "https://developer.apple.com/documentation/swiftui/color", "10.15", 85, setOf("SolidColor"), false),
    UiComponent("Divider", "layout-fundamentals", "https://developer.apple.com/documentation/swiftui/divider", "10.15", 86, setOf("Divider"), false),
    UiComponent("Group", "view-groupings", "https://developer.apple.com/documentation/swiftui/group", "10.15", 81, setOf("Group"), false),
    UiComponent("RoundedRectangle", "shapes", "https://developer.apple.com/documentation/swiftui/roundedrectangle", "10.15", 77, setOf("RoundedRectangle"), false),
    UiComponent("Label", "text-input-and-output", "https://developer.apple.com/documentation/swiftui/label", "11.0", 72, setOf("Label"), false),
    UiComponent("Rectangle", "shapes", "https://developer.apple.com/documentation/swiftui/rectangle", "10.15", 62, setOf("Rectangle"), false),
    UiComponent("EmptyView", "view-fundamentals", "https://developer.apple.com/documentation/swiftui/emptyview", "10.15", 60, setOf("EmptyView"), false),
    UiComponent("Form", "view-groupings", "https://developer.apple.com/documentation/swiftui/form", "10.15", 57, setOf("Form"), false),
    UiComponent("Toggle", "controls-and-indicators", "https://developer.apple.com/documentation/swiftui/toggle", "10.15", 56, setOf("Toggle"), false),
    UiComponent("ZStack", "layout-fundamentals", "https://developer.apple.com/documentation/swiftui/zstack", "10.15", 52, setOf("Box"), false),
    UiComponent("TextField", "text-input-and-output", "https://developer.apple.com/documentation/swiftui/textfield", "10.15", 50, setOf("TextField"), false),
    UiComponent("ProgressView", "controls-and-indicators", "https://developer.apple.com/documentation/swiftui/progressview", "11.0", 47, setOf("ProgressView"), false),
    UiComponent("Picker", "controls-and-indicators", "https://developer.apple.com/documentation/swiftui/picker", "10.15", 45, setOf("Picker", "Tag", "PickerStyle"), false),
    UiComponent("ToolbarItem", "toolbars", "https://developer.apple.com/documentation/swiftui/toolbaritem", "11.0", 42, setOf("ToolbarItem"), false),
    UiComponent("List", "lists", "https://developer.apple.com/documentation/swiftui/list", "10.15", 41, setOf("NativeList"), false),
    UiComponent("Menu", "menus-and-commands", "https://developer.apple.com/documentation/swiftui/menu", "11.0", 37, setOf("Menu"), false),
    UiComponent("NavigationStack", "navigation", "https://developer.apple.com/documentation/swiftui/navigationstack", "13.0", 36, setOf("NavigationStack"), false),
    UiComponent("ScrollView", "scroll-views", "https://developer.apple.com/documentation/swiftui/scrollview", "10.15", 33, setOf("ScrollView"), false),
    UiComponent("Circle", "shapes", "https://developer.apple.com/documentation/swiftui/circle", "10.15", 27, setOf("Circle"), false),
    UiComponent("GeometryReader", "drawing-and-graphics", "https://developer.apple.com/documentation/swiftui/geometryreader", "10.15", 20, setOf("GeometryReader"), false),
    UiComponent("Capsule", "shapes", "https://developer.apple.com/documentation/swiftui/capsule", "10.15", 16, setOf("Capsule"), false),
    UiComponent("LabeledContent", "view-groupings", "https://developer.apple.com/documentation/swiftui/labeledcontent", "13.0", 13, setOf("LabeledContent"), false),
    UiComponent("LinearGradient", "drawing-and-graphics", "https://developer.apple.com/documentation/swiftui/lineargradient", "10.15", 13, setOf("LinearGradientColors"), false),
    UiComponent("NavigationLink", "navigation", "https://developer.apple.com/documentation/swiftui/navigationlink", "10.15", 13, setOf("NavigationLink"), false),
    UiComponent("ScrollViewReader", "scroll-views", "https://developer.apple.com/documentation/swiftui/scrollviewreader", "11.0", 9, setOf("ScrollViewReader", "Id"), false),
    UiComponent("LazyVStack", "layout-fundamentals", "https://developer.apple.com/documentation/swiftui/lazyvstack", "11.0", 8, setOf("LazyColumn"), false),
    UiComponent("LazyHStack", "layout-fundamentals", "https://developer.apple.com/documentation/swiftui/lazyhstack", "11.0", 7, setOf("LazyRow"), false),
    UiComponent("Link", "controls-and-indicators", "https://developer.apple.com/documentation/swiftui/link", "11.0", 7, setOf("Link"), false),
    UiComponent("ToolbarItemGroup", "toolbars", "https://developer.apple.com/documentation/swiftui/toolbaritemgroup", "11.0", 6, setOf("ToolbarItemGroup"), false),
    UiComponent("LazyVGrid", "layout-fundamentals", "https://developer.apple.com/documentation/swiftui/lazyvgrid", "11.0", 5, setOf("LazyVGrid"), false),
    UiComponent("ShareLink", "controls-and-indicators", "https://developer.apple.com/documentation/swiftui/sharelink", "13.0", 5, setOf("ShareLink"), false),
    UiComponent("ToolbarSpacer", "toolbars", "https://developer.apple.com/documentation/swiftui/toolbarspacer", "26.0", 5, setOf("ToolbarSpacer"), false),
    UiComponent("AsyncImage", "images", "https://developer.apple.com/documentation/swiftui/asyncimage", "12.0", 4, setOf("AsyncImage"), false),
    UiComponent("ContainerRelativeShape", "shapes", "https://developer.apple.com/documentation/swiftui/containerrelativeshape", "11.0", 4, setOf("ContainerRelativeShape"), false),
    UiComponent("SecureField", "text-input-and-output", "https://developer.apple.com/documentation/swiftui/securefield", "10.15", 4, setOf("SecureField"), false),
    UiComponent("ColorPicker", "controls-and-indicators", "https://developer.apple.com/documentation/swiftui/colorpicker", "11.0", 3, setOf("ColorPicker"), false),
    UiComponent("ContentUnavailableView", "controls-and-indicators", "https://developer.apple.com/documentation/swiftui/contentunavailableview", "14.0", 3, setOf("ContentUnavailableView"), false),
    UiComponent("EditButton", "controls-and-indicators", "https://developer.apple.com/documentation/swiftui/editbutton", null, 3, setOf(), false),
    UiComponent("NavigationSplitView", "navigation", "https://developer.apple.com/documentation/swiftui/navigationsplitview", "13.0", 3, setOf("NavigationSplitView"), false),
    UiComponent("Slider", "controls-and-indicators", "https://developer.apple.com/documentation/swiftui/slider", "10.15", 3, setOf("Slider"), false),
    UiComponent("Stepper", "controls-and-indicators", "https://developer.apple.com/documentation/swiftui/stepper", "10.15", 3, setOf("Stepper"), false),
    UiComponent("Chart", "technology-specific-views", "https://developer.apple.com/documentation/charts/chart", "13.0", 2, setOf("Chart"), false),
    UiComponent("ControlGroup", "view-groupings", "https://developer.apple.com/documentation/swiftui/controlgroup", "12.0", 2, setOf("ControlGroup"), false),
    UiComponent("GroupBox", "view-groupings", "https://developer.apple.com/documentation/swiftui/groupbox", "10.15", 2, setOf("GroupBox"), false),
    UiComponent("Path", "shapes", "https://developer.apple.com/documentation/swiftui/path", "10.15", 2, setOf("PathFillGradient", "PathStroke"), false),
    UiComponent("TabView", "navigation", "https://developer.apple.com/documentation/swiftui/tabview", "10.15", 2, setOf("SidebarTabs", "TabView", "Tab", "TabSection"), false),
    UiComponent("TimelineView", "animations", "https://developer.apple.com/documentation/swiftui/timelineview", "12.0", 2, setOf("TimelineView"), false),
    UiComponent("ToolbarTitleMenu", "toolbars", "https://developer.apple.com/documentation/swiftui/toolbartitlemenu", "13.0", 2, setOf("ToolbarTitleMenu"), false),
    UiComponent("AngularGradient", "drawing-and-graphics", "https://developer.apple.com/documentation/swiftui/angulargradient", "10.15", 1, setOf("AngularGradientColors"), false),
    UiComponent("DatePicker", "controls-and-indicators", "https://developer.apple.com/documentation/swiftui/datepicker", "10.15", 1, setOf("DatePicker"), false),
    UiComponent("DisclosureGroup", "lists", "https://developer.apple.com/documentation/swiftui/disclosuregroup", "11.0", 1, setOf("DisclosureGroup"), false),
    UiComponent("HelpLink", "controls-and-indicators", "https://developer.apple.com/documentation/swiftui/helplink", "14.0", 1, setOf("HelpLink"), false),
    UiComponent("Tab", "navigation", "https://developer.apple.com/documentation/swiftui/tab", "15.0", 1, setOf("Tab"), false),
    UiComponent("TabSection", "navigation", "https://developer.apple.com/documentation/swiftui/tabsection", "15.0", 1, setOf("TabSection"), false),
    UiComponent("Table", "tables", "https://developer.apple.com/documentation/swiftui/table", "12.0", 1, setOf("Table", "TableColumn", "TableRow", "TableCell"), false),
    UiComponent("TableColumn", "tables", "https://developer.apple.com/documentation/swiftui/tablecolumn", "12.0", 1, setOf("TableColumn"), false),
    UiComponent("TextEditor", "text-input-and-output", "https://developer.apple.com/documentation/swiftui/texteditor", "11.0", 1, setOf("TextEditor"), false),
    UiComponent("UnevenRoundedRectangle", "shapes", "https://developer.apple.com/documentation/swiftui/unevenroundedrectangle", "13.0", 1, setOf("UnevenRoundedRectangle"), false),
    UiComponent("VideoPlayer", "technology-specific-views", "https://developer.apple.com/documentation/avkit/videoplayer", "11.0", 1, setOf("VideoPlayer"), false),
    UiComponent("ViewThatFits", "layout-fundamentals", "https://developer.apple.com/documentation/swiftui/viewthatfits", "13.0", 1, setOf("ViewThatFits"), false),
    UiComponent("AddPassToWalletButton", "technology-specific-views", "https://developer.apple.com/documentation/passkit/addpasstowalletbutton", null, 0, setOf(), false),
    UiComponent("ArrangementView", "layout-fundamentals", "https://developer.apple.com/documentation/swiftui/arrangementview", "27.1", 0, setOf(), false),
    UiComponent("CameraView", "technology-specific-views", "https://developer.apple.com/documentation/homekit/cameraview", null, 0, setOf(), false),
    UiComponent("Canvas", "drawing-and-graphics", "https://developer.apple.com/documentation/swiftui/canvas", "12.0", 0, setOf("CanvasStroke"), true),
    UiComponent("ConcentricRectangle", "shapes", "https://developer.apple.com/documentation/swiftui/concentricrectangle", "26.0", 0, setOf("ConcentricRectangle"), false),
    UiComponent("DeviceActivityReport", "technology-specific-views", "https://developer.apple.com/documentation/deviceactivity/deviceactivityreport", null, 0, setOf(), false),
    UiComponent("DevicePicker", "technology-specific-views", "https://developer.apple.com/documentation/devicediscoveryui/devicepicker", null, 0, setOf(), false),
    UiComponent("DisclosureTableRow", "tables", "https://developer.apple.com/documentation/swiftui/disclosuretablerow", "14.0", 0, setOf(), false),
    UiComponent("DocumentLaunchView", "documents", "https://developer.apple.com/documentation/swiftui/documentlaunchview", null, 0, setOf(), false),
    UiComponent("Ellipse", "shapes", "https://developer.apple.com/documentation/swiftui/ellipse", "10.15", 0, setOf("Ellipse"), false),
    UiComponent("EllipticalGradient", "drawing-and-graphics", "https://developer.apple.com/documentation/swiftui/ellipticalgradient", "12.0", 0, setOf("EllipticalGradientColors"), false),
    UiComponent("FamilyActivityPicker", "technology-specific-views", "https://developer.apple.com/documentation/familycontrols/familyactivitypicker", null, 0, setOf(), false),
    UiComponent("Gauge", "controls-and-indicators", "https://developer.apple.com/documentation/swiftui/gauge", "13.0", 0, setOf("Gauge"), false),
    UiComponent("GeometryReader3D", "drawing-and-graphics", "https://developer.apple.com/documentation/swiftui/geometryreader3d", null, 0, setOf(), false),
    UiComponent("Grid", "layout-fundamentals", "https://developer.apple.com/documentation/swiftui/grid", "13.0", 0, setOf("Grid"), false),
    UiComponent("GridRow", "layout-fundamentals", "https://developer.apple.com/documentation/swiftui/gridrow", "13.0", 0, setOf("GridRow"), false),
    UiComponent("HSplitView", "navigation", "https://developer.apple.com/documentation/swiftui/hsplitview", "10.15", 0, setOf("HSplitView"), false),
    UiComponent("KeyframeAnimator", "animations", "https://developer.apple.com/documentation/swiftui/keyframeanimator", "14.0", 0, setOf(), false),
    UiComponent("LazyHGrid", "layout-fundamentals", "https://developer.apple.com/documentation/swiftui/lazyhgrid", "11.0", 0, setOf("LazyHGrid"), false),
    UiComponent("LocalAuthenticationView", "technology-specific-views", "https://developer.apple.com/documentation/localauthentication/localauthenticationview", "13.0", 0, setOf(), false),
    UiComponent("LocationButton", "technology-specific-views", "https://developer.apple.com/documentation/corelocationui/locationbutton", null, 0, setOf(), false),
    UiComponent("Map", "technology-specific-views", "https://developer.apple.com/documentation/mapkit/map", "11.0", 0, setOf("Map"), false),
    UiComponent("MenuButton", "menus-and-commands", "https://developer.apple.com/documentation/swiftui/menubutton", "10.15", 0, setOf(), false),
    UiComponent("MeshGradient", "drawing-and-graphics", "https://developer.apple.com/documentation/swiftui/meshgradient", "15.0", 0, setOf("MeshGradient"), false),
    UiComponent("MultiDatePicker", "controls-and-indicators", "https://developer.apple.com/documentation/swiftui/multidatepicker", null, 0, setOf(), false),
    UiComponent("NavigationView", "navigation", "https://developer.apple.com/documentation/swiftui/navigationview", "10.15", 0, setOf("NavigationView"), false),
    UiComponent("NewDocumentButton", "documents", "https://developer.apple.com/documentation/swiftui/newdocumentbutton", "15.0", 0, setOf(), false),
    UiComponent("NowPlayingView", "technology-specific-views", "https://developer.apple.com/documentation/watchkit/nowplayingview", null, 0, setOf(), false),
    UiComponent("OutlineGroup", "lists", "https://developer.apple.com/documentation/swiftui/outlinegroup", "11.0", 0, setOf(), false),
    UiComponent("PasteButton", "controls-and-indicators", "https://developer.apple.com/documentation/swiftui/pastebutton", "10.15", 0, setOf(), false),
    UiComponent("PayWithApplePayButton", "technology-specific-views", "https://developer.apple.com/documentation/passkit/paywithapplepaybutton", "13.0", 0, setOf(), false),
    UiComponent("PhaseAnimator", "animations", "https://developer.apple.com/documentation/swiftui/phaseanimator", "14.0", 0, setOf(), false),
    UiComponent("PhotosPicker", "technology-specific-views", "https://developer.apple.com/documentation/photosui/photospicker", "13.0", 0, setOf(), false),
    UiComponent("RadialGradient", "drawing-and-graphics", "https://developer.apple.com/documentation/swiftui/radialgradient", "10.15", 0, setOf("RadialGradientColors"), false),
    UiComponent("RenameButton", "controls-and-indicators", "https://developer.apple.com/documentation/swiftui/renamebutton", "13.0", 0, setOf("RenameButton"), false),
    UiComponent("SceneView", "technology-specific-views", "https://developer.apple.com/documentation/scenekit/sceneview", "11.0", 0, setOf(), false),
    UiComponent("SignInWithAppleButton", "technology-specific-views", "https://developer.apple.com/documentation/authenticationservices/signinwithapplebutton", "11.0", 0, setOf(), false),
    UiComponent("SpriteView", "technology-specific-views", "https://developer.apple.com/documentation/spritekit/spriteview", "11.0", 0, setOf(), false),
    UiComponent("TableRow", "tables", "https://developer.apple.com/documentation/swiftui/tablerow", "12.0", 0, setOf("TableRow"), false),
    UiComponent("TextFieldLink", "controls-and-indicators", "https://developer.apple.com/documentation/swiftui/textfieldlink", null, 0, setOf(), false),
    UiComponent("ToolbarOverflowMenu", "toolbars", "https://developer.apple.com/documentation/swiftui/toolbaroverflowmenu", null, 0, setOf(), false),
    UiComponent("VSplitView", "navigation", "https://developer.apple.com/documentation/swiftui/vsplitview", "10.15", 0, setOf("VSplitView"), false),
    UiComponent("VerifyIdentityWithWalletButton", "technology-specific-views", "https://developer.apple.com/documentation/passkit/verifyidentitywithwalletbutton", null, 0, setOf(), false),
    UiComponent("WebView", "technology-specific-views", "https://developer.apple.com/documentation/webkit/webview-swift.struct", "26.0", 0, setOf(), false),
    UiComponent("WindowVisibilityToggle", "windows", "https://developer.apple.com/documentation/swiftui/windowvisibilitytoggle", "15.0", 0, setOf(), false),
)
// @formatter:on

internal val kotlinCompositionHelpers = setOf("ForEach")

fun availableComponents(selectedAdapters: Set<String>): List<UiComponent> =
    nativeUiCatalog.filter { !it.partial && it.adapters.isNotEmpty() && selectedAdapters.containsAll(it.adapters) }

fun printControlCoverage() {
    val selected = bindings.map { it.name }.toSet() + kotlinCompositionHelpers
    val implemented = availableComponents(selected)
    val threshold = (nativeUiCatalog.size * 7 + 9) / 10
    println(
        "Official component inventory: ${nativeUiCatalog.size}; 70% threshold: $threshold; components with available adapters: ${implemented.size}",
    )
    println("The inventory includes all official platforms and related framework UI; platform-specific gaps remain in the denominator.")
    println("Binding presence requires separate native behavior and performance verification.")
    println("Usage counts match component references in SwiftUI source files, excluding comments and strings; they are approximate.")
    controlUsageSources.forEach { println("Usage source: ${it.repository}@${it.commit} (${it.swiftFiles} Swift files)") }
    nativeUiCatalog.forEach { component ->
        val state =
            if (component in implemented) {
                "bound"
            } else if (component.partial) {
                "partial"
            } else {
                "missing"
            }
        println("${component.name}\t$state\t${component.sourceFiles} source files\t${component.documentation}")
    }
}
