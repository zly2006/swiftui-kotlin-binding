# swiftui-kotlin-binding

通过 Kotlin 和 Compose 声明式语法使用 Apple 原生 UI，目标是覆盖实际应用需要的原生能力，并保留原生按需渲染和低资源占用的优势。包名前缀为 `me.zly2006`，当前使用 Kotlin 2.4.0、Compose Runtime 1.11.1。

Kotlin 负责 UI 声明、布局意图、视觉参数、状态和行为；Swift 只做官方 API 的薄层转接。重复绑定由关键组件白名单和有类型模型生成，不传运行时 JSON 页面，也不全量导出 SDK 符号。当前支持 macOS Apple Silicon；JVM target 用于公共 API 与组合更新测试。

## 模块

项目保留三个库模块和两个测试模块，职责与依赖见 [modules.md](docs/modules.md)。旧 Skia 混合宿主和四类手写绑定已移除。

- `binding-generator`：白名单与语义模型的构建期生成器。
- `binding-core`：SwiftUI 原生宿主、必要语义适配、动态库和 C ABI。
- `native-compose`：公共 Kotlin Composable API 与 Compose Runtime 接入。
- `samples:demo-ui`：纯 Kotlin 测试 UI；外部应用样本仅本地保留。
- `samples:native-macos`：macOS 窗口、应用生命周期和打包测试入口。

## 构建与验证

macOS、Xcode 与 arm64 JDK 21 环境下：

```sh
export JAVA_HOME="$(/usr/libexec/java_home -v 21 -a arm64)"
./gradlew :binding-generator:test :native-compose:jvmTest :samples:native-macos:linkDebugExecutableMacosArm64
```

本地打包和自身窗口验证使用 `scripts/build-native.sh` 与 `scripts/verify-native.sh`。外部应用的源码、资源、参考图片及应用包不提交，测试样本不属于公开库依赖。

## 维护依据

- [项目目标和完成标准](docs/project-goal.md)
- [Kotlin、Swift 与模块的责任边界](docs/architecture.md)
- [关键组件白名单](docs/component-whitelist.md)
- [代码生成](docs/code-generation.md)
- [原生验证记录](docs/native-verification.md)
- [项目约束](AGENTS.md)

项目仍在开发中。已生成或编译的绑定不等于完整控件行为已经验证；像素一致性和低占用分别需要真实截图与等价原生性能对照。
