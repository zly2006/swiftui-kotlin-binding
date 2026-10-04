# Getting started

Build a native macOS interface with Kotlin Composables and the runtime artifact.

## Requirements

Use an Apple Silicon Mac, a compatible Apple SDK/toolchain, Kotlin 2.4.0, and an arm64 JDK 21. The native bridge targets macOS 15 or later; the release was tested with Xcode 26 on macOS 26. Compose Runtime 1.11.1 resolves transitively.

## Configure a project

Add this `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories { mavenCentral(); gradlePluginPortal(); google() }
}
dependencyResolutionManagement {
    repositories { mavenCentral(); google() }
}
rootProject.name = "native-counter"
```

Use this `build.gradle.kts`:

```kotlin
plugins {
    kotlin("multiplatform") version "2.4.0"
    kotlin("plugin.compose") version "2.4.0"
    id("org.jetbrains.compose") version "1.11.1"
}
kotlin {
    macosArm64 {
        binaries.executable {
            baseName = "NativeCounter"
            entryPoint = "example.main"
        }
    }
    sourceSets.commonMain.dependencies {
        implementation("me.zly2006:swiftui-compose:0.1.0")
    }
}
```

Add `org.jetbrains.compose.experimental.macos.enabled=true` to `gradle.properties`. Use the same Kotlin version as the library. The native bridge and embedded Swift archive resolve transitively; consumers do not generate bindings or copy a dylib from the library's checkout.

## Define the UI

Create `src/commonMain/kotlin/example/Counter.kt`:

```kotlin
package example

import androidx.compose.runtime.*
import me.zly2006.swiftui.nativeui.*

@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    Column(spacing = 12.0, modifier = NativeModifier.padding(24.0)) {
        Text("Count: $count", NativeModifier.semanticFont(TextStyle.Headline))
        Button(onClick = { count++ }) { Text("Increment") }
    }
}
```

`Row`, `Column`, and `Box` call native stack containers. `Text`, `Button`, and `TextEditor` call official SwiftUI controls. Modifier order follows SwiftUI: padding before a background includes the padding in that background.

## Open a native window

Create `src/macosMain/kotlin/example/Main.kt`:

```kotlin
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
package example

import kotlinx.cinterop.autoreleasepool
import me.zly2006.swiftui.nativeui.*
import platform.AppKit.*
import platform.Foundation.*
import platform.darwin.NSObject

private class WindowDelegate : NSObject(), NSWindowDelegateProtocol {
    override fun windowWillClose(notification: NSNotification) {
        NSApplication.sharedApplication().stop(null)
    }
}

fun main() = autoreleasepool {
    val app = NSApplication.sharedApplication()
    app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)
    app.finishLaunching()
    val backend = MacosNativeUiBackend()
    val composition = NativeComposition(backend)
    composition.setContent { Counter() }
    val host = backend.createHost(composition.rootElement)
    val window = NSWindow(
        NSMakeRect(0.0, 0.0, 360.0, 220.0),
        NSWindowStyleMaskTitled or NSWindowStyleMaskClosable,
        NSBackingStoreBuffered, false,
    )
    window.releasedWhenClosed = false
    window.title = "Native Counter"
    window.contentView = host.nsView
    val delegate = WindowDelegate()
    window.delegate = delegate
    window.makeKeyAndOrderFront(null)
    app.run()
    window.contentView = null
    host.close()
    composition.close()
    backend.close()
}
```

All native creation, updates, and release run on the main thread. Keep the delegate alive during the event loop. On exit, detach the view, close the host, dispose the composition, then close the backend. Kotlin/Native's Objective-C wrappers may retain objects until their scope ends.

Build with `gradle linkDebugExecutableMacosArm64`, then run `build/bin/macosArm64/debugExecutable/NativeCounter.kexe`. A Gradle wrapper can be used in place of the installed `gradle` command.

## Support

This is an early macOS arm64 release. The [component whitelist](component-whitelist.md) records supported bindings and pending capabilities. JVM artifacts support common API compilation and tests, not Apple UI rendering.
