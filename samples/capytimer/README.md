# CapyTimer

An offline Kotlin/Native UI mock with native SwiftUI controls.

![CapyTimer in dark mode](screenshots/preview.png)

## Key code

- [CapyTimerMock.kt](src/commonMain/kotlin/me/zly2006/swiftui/samples/capytimer/CapyTimerMock.kt) declares the timer ring, task list, notes editor, and settings with Kotlin Composables. `CapyState` handles local actions; the timer stays fixed for the mock.
- [Main.kt](src/macosMain/kotlin/me/zly2006/swiftui/samples/capytimer/Main.kt) opens the window through the shared [sample host](../sample-host/src/macosMain/kotlin/me/zly2006/swiftui/samples/host/SampleHost.kt).

## Credits

Based on [CapyTimer by anvndev (@andev0x)](https://github.com/andev0x/CapyTimer/tree/96078520a9c80e8114f30fec127068437bd56eac); upstream view source credits Andeph Nguyen. Copyright © 2025 anvndev (@andev0x). MIT license; the original notice is preserved in [LICENSE](LICENSE).
