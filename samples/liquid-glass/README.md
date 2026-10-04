# Liquid Glass

An offline Kotlin/Native UI mock with six dark themes and a native SwiftUI glass card.

![Liquid Glass in the Cyberpunk theme](screenshots/preview.png)

## Key code

- [LiquidGlassMock.kt](src/commonMain/kotlin/me/zly2006/swiftui/samples/liquidglass/LiquidGlassMock.kt) declares the layout, themes, mesh, grid, blur, and icon paths in Kotlin Composables. `GlassState` handles theme selection, hover, and the Inspector toggle.
- [Main.kt](src/macosMain/kotlin/me/zly2006/swiftui/samples/liquidglass/Main.kt) opens the window through the shared [sample host](../sample-host/src/macosMain/kotlin/me/zly2006/swiftui/samples/host/SampleHost.kt). The mock uses the upstream snapshot fallback for the card; the full settings modal is omitted.

## Credits

Based on [LiquidGlassDemo by Sohrab Sheikhani](https://github.com/SohrabZ/swiftui-macos-app/tree/fb677fb68686f429aeb6882beb96caeeb8b39c29). Copyright © 2026 Sohrab Sheikhani. MIT license; the original notice is preserved in [LICENSE](LICENSE).
