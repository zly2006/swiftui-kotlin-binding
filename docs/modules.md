# 当前模块职责

项目保留两个运行库模块和一个构建期生成工具，另有通用测试界面、宿主及独立开源 UI 示例。旧 Compose/Skia 实验、对应示例及四类手写组件绑定已经删除；当前只有原生 SwiftUI 渲染路径。

| 模块 | 职责 | 主要输入与输出 |
| --- | --- | --- |
| `swiftui-codegen` | 构建期代码生成。根据白名单和语义模型生成逐项接口，避免人工同步重复声明。 | 输入为 `Whitelist.kt` 和 `Model.kt`；输出为 `build/generated/native-ui/` 下的 C 头文件、Swift 转发、公共 Kotlin API、macOS 绑定及 Composable。它不参与应用运行，也不发布到 Maven。 |
| `swiftui-bridge` | macOS 原生桥的编译与 C 互操作。保留必要的 SwiftUI 宿主、原生节点、子内容适配及资源持有逻辑。 | 将 `NativeTreeRuntime.swift` 与生成的 Swift 编译为静态桥，并内嵌到发布的 cinterop `.klib`；通过生成的 `NativeUI.h` 导入 C ABI，统一检查主线程。旧的动态库任务仍供本地打包使用。没有 Compose、应用页面或 JVM 渲染实现。 |
| `swiftui-compose` | 面向 Kotlin 使用者的运行库。提供生成的原生 Composable API、状态与重组接入、节点更新、回调和所有权管理。 | 依赖 Compose Runtime；macOS 实现依赖 `swiftui-bridge`。公共 UI API 不暴露 C 指针。JVM target 用于公共 API 编译和接入测试，不提供 Apple UI 渲染。 |
| `samples:demo-ui` | 独立的纯 Kotlin 测试界面，包含本项目编写的通用控件示例；外部应用的 UI 可在本地挂载。 | 只依赖 `swiftui-compose`，不导入 AppKit、cinterop 或 Swift。外部应用的测试源码和资源被忽略，不进入提交。 |
| `samples:native-macos` | macOS 测试应用宿主。负责 NSApplication、窗口、外观、链接、打包和自身窗口的截图。 | 消费测试 UI 和 `swiftui-compose`，生成可执行文件；本地应用包和构建产物不提交。它不承担库的公共组件实现。 |
| `samples:sample-host` | 独立样本共用的 Kotlin/macOS 窗口宿主。 | 创建一个原生根宿主，处理窗口生命周期、自身截图、静止调度检查及资源释放；不包含样本页面。 |
| `samples:capytimer` | CapyTimer 主面板的离线 Kotlin UI mock。 | 公共部分使用 Composable 编写进度环、任务、Notes 和设置；macOS 入口调用 `sample-host`。 |
| `samples:liquid-glass` | LiquidGlassDemo 三栏界面的离线 Kotlin UI mock。 | 公共部分声明主题、网格、渐变、图标路径和卡片；macOS 入口调用 `sample-host`。 |

构建期依赖为 `swiftui-codegen` → 生成代码 → `swiftui-bridge` 与 `swiftui-compose`。

运行时依赖为 `samples:native-macos` → `samples:demo-ui` → `swiftui-compose` → macOS 的 `swiftui-bridge`。测试宿主也直接使用 `swiftui-compose` 管理根组合与窗口视图。JVM 测试不再依赖 `swiftui-bridge`，公共 API 与 Apple 平台绑定已经分开。

Kotlin 决定组合结构、布局意图、视觉参数、状态和行为；生成的 Swift 转发只调用官方原生组件与 modifier。原生框架执行实际测量与绘制。完整责任边界以 [architecture.md](architecture.md) 为准。

两个独立开源样本由用户明确授权提交，均按上游 MIT 许可证保留独立 `LICENSE`，在各自 README 中列出作者、固定源码版本、Kotlin 关键代码和实际运行截图。库模块不依赖样本；上游 Swift 源码、参考应用和本地对照材料继续保留在被忽略的 `work/` 中，构建产物不提交。
