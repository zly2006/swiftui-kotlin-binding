<div align="center">

# SwiftUI Kotlin Binding

**Write native SwiftUI in Kotlin.**

Compose syntax. Kotlin state. Apple-rendered controls.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/) [![Compose Runtime](https://img.shields.io/badge/Compose_Runtime-1.11.1-4285F4)](https://www.jetbrains.com/compose-multiplatform/) [![Platform](https://img.shields.io/badge/platform-macOS_arm64-111111?logo=apple)](#support) [![License](https://img.shields.io/badge/license-GPL_v3-blue)](LICENSE) [![Maven Central](https://img.shields.io/badge/Maven_Central-0.1.0-2b6cb0)](https://central.sonatype.com/artifact/me.zly2006.swiftui/swiftui-compose/0.1.0)

**English** · [简体中文](README.zh-CN.md)

[Quick start](#quick-start) · [Samples](#samples) · [How it works](#how-it-works) · [Documentation](#documentation)

</div>

SwiftUI Kotlin Binding lets you build Apple-native interfaces with Kotlin Composables. Kotlin owns the layout, styling, state, and behavior; a small generated bridge calls official SwiftUI APIs. Compose Runtime manages composition and updates, while SwiftUI measures and draws the interface.

## Samples

Real screenshots of the Kotlin/Native applications. Click an image to explore its sample.

<table>
<tr>
<th width="25%"><a href="samples/capytimer/">CapyTimer</a></th>
<th width="75%"><a href="samples/liquid-glass/">Liquid Glass</a></th>
</tr>
<tr>
<td valign="top"><a href="samples/capytimer/"><img src="samples/capytimer/screenshots/preview.png" alt="CapyTimer: a native timer, task list, notes editor, and settings" width="220"></a><p>A compact focus panel with a progress ring, tasks, and editable notes.</p></td>
<td valign="top"><a href="samples/liquid-glass/"><img src="samples/liquid-glass/screenshots/preview.png" alt="Liquid Glass: the Cyberpunk theme with a mesh backdrop and frosted card" width="760"></a><p>Six dark themes, a mesh backdrop, a frosted card, and an icon made from Kotlin-defined paths and gradients.</p><p>Both samples use local mock data and native controls. Original projects and MIT notices are credited in their directories.</p></td>
</tr>
</table>

## Why this library?

- **Native rendering.** Text, buttons, editors, layout containers, and effects call official SwiftUI APIs.
- **Kotlin-owned UI.** Declare your interface with `@Composable`, manage state with `remember`, and handle actions in Kotlin.
- **One native root.** A single `NSHostingView` hosts the composed tree. State changes update existing nodes.
- **Typed, generated bindings.** A reviewed component whitelist produces C ABI, Swift forwarding, Kotlin APIs, and Composables from one model.
- **Updates when needed.** The runtime schedules composition work for state changes. Static sample windows pass idle scheduling and resource-release checks.

## Quick start

### Run a sample

On an Apple Silicon Mac with Xcode and an arm64 JDK 21:

```sh
git clone https://github.com/zly2006/swiftui-kotlin-binding.git
cd swiftui-kotlin-binding
export JAVA_HOME="$(/usr/libexec/java_home -v 21 -a arm64)"
./gradlew :samples:liquid-glass:linkDebugExecutableMacosArm64
./samples/liquid-glass/build/bin/macosArm64/debugExecutable/LiquidGlassMock.kexe --dark
```

For CapyTimer, use `:samples:capytimer:linkDebugExecutableMacosArm64` and `CapyTimerMock.kexe` in that sample's build directory.

### Add it to your Kotlin project

Version **0.1.0** is available from Maven Central; use its [release guide](https://github.com/zly2006/swiftui-kotlin-binding/blob/swiftui-v0.1.0/docs/getting-started.md) for that API. The examples below use the current source API with Compose `Modifier` (`0.2.0-SNAPSHOT`).

After cloning this repository, add `includeBuild("../swiftui-kotlin-binding")` to your application’s `settings.gradle.kts`, adjusting the path to your checkout.

Use Kotlin **2.4.0**, the Kotlin Compose compiler plugin, and a `macosArm64()` target. Add the runtime dependency:

```kotlin
repositories { mavenCentral() }

kotlin {
    macosArm64()
    sourceSets.commonMain.dependencies {
        implementation("me.zly2006.swiftui:swiftui-compose:0.2.0-SNAPSHOT")
    }
}
```

The macOS bridge and its native static library resolve transitively. You do not need to generate bindings or copy a Swift library to consume the release.

### Write your UI

```kotlin
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import me.zly2006.swiftui.nativeui.*

@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    VStack(spacing = 12.0, modifier = Modifier.padding(24.0)) {
        Text("Count: $count", Modifier.font(Font.TextStyle.headline))
        Button(onClick = { count++ }) { Text("Increment") }
    }
}
```

Host this content in a macOS window with `MacosNativeUiBackend` and `NativeComposition`. See the complete [getting started guide](docs/getting-started.md) for project configuration, a window entry point, and lifecycle cleanup.

## How it works

```text
Kotlin @Composable + state
          ↓
Compose Runtime → incremental native node updates
          ↓
Generated typed C ABI → thin Swift forwarding
          ↓
Official SwiftUI → one native window host
```

| Module | Responsibility |
| --- | --- |
| [`swiftui-compose`](swiftui-compose/) | Public Kotlin Composables, Compose Runtime integration, updates, callbacks, and ownership. This is the dependency applications use. |
| [`swiftui-bridge`](swiftui-bridge/) | Native interoperability, SwiftUI hosting, resource management, and the embedded Swift static library. |
| [`swiftui-codegen`](swiftui-codegen/) | Repository build tool for generation from the component whitelist and typed semantic model; not published to Maven. |
| [`samples`](samples/) | Independent Kotlin UI examples and macOS test hosts. |

Kotlin chooses the UI and its parameters. Swift forwards official API calls; it contains no application pages or business state. The rendering path uses Compose Runtime, with no Skia drawing host.

## Support

**0.1.0 is an early release for macOS on Apple Silicon.** Native APIs target macOS 15 or later. Build applications with Kotlin 2.4.0 and a compatible Apple toolchain; this release was tested on macOS 26 with Xcode 26.

The JVM target supports public API compilation and runtime tests; it does not render Apple UI. iOS, Intel macOS, and full SwiftUI API coverage are future work. Native rendering is intended to retain the platform's resource advantages; comparative CPU and memory benchmarks remain to be completed.

## Documentation

- [Getting started](docs/getting-started.md) — configure a project and open a native window.
- [Architecture](docs/architecture.md) — the Kotlin/Swift boundary and native view tree.
- [Extending the bindings](docs/code-generation.md) — add a native capability.
- [Project goal](docs/project-goal.md) — future capability and performance work.

Contributions to API coverage, native behavior, and documentation are welcome. Follow the [project constraints](AGENTS.md) and run:

```sh
./gradlew :swiftui-codegen:test :swiftui-compose:jvmTest
```

## License

The library is licensed under **GPL-3.0-only**. See [LICENSE](LICENSE). The CapyTimer and Liquid Glass adaptations retain their upstream MIT licenses and credits in their respective sample directories.
