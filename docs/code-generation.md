# 白名单与代码生成方案

生成器从关键组件白名单开始工作，依据官方文档确定组件和必要调用，再用开源应用的真实用法查漏补缺。白名单和本轮核读证据见 [component-whitelist.md](component-whitelist.md)。禁止全量导出 SDK 符号，也不需要为每个函数、协议、重载和框架辅助类型生成绑定。

## 选择必要能力

先判断原生组件及其组合能否满足 UI 需求，再选择缺失的必要调用。白名单在 `swiftui-codegen/src/main/kotlin/me/zly2006/swiftui/generator/Whitelist.kt` 中定义。常用组件、布局、输入、导航和效果按类别登记；尚未实现的需求明确保留，不能因为生成器只支持一部分就从完成标准中消失。

只针对所选接口查看官方文档、本机 SDK 声明和 availability，最后由 Swift 编译器核对调用。该过程没有 symbol graph 全量导出或全 SDK 审计任务。

官方 Kotlin 文档说明，纯 Swift 模块尚不能直接通过现有 Objective-C 互操作导入。SwiftUI 泛型 View、关联类型和 ViewBuilder 需要薄适配；cinterop 不会自动导入所有 SwiftUI 语义。参见 [Swift/Objective-C 互操作](https://kotlinlang.org/docs/native-objc-interop.html)。

## 同一份语义模型

`Model.kt` 定义所选原生能力的参数、值类型、枚举、回调、子内容和官方调用适配。`Emitter.kt` 根据模型生成 C ABI、Swift 官方 API 转发、Kotlin API、macOS 绑定和原生 Composable 声明。未进入白名单的适配定义不会生成，白名单没有适配定义时生成失败。

模型负责空值、颜色、尺寸、枚举和有类型回调的转换。布局参数、视觉配置默认值、应用状态与行为留在 Kotlin；Swift 只执行官方 API 的必要调用。复杂 ViewBuilder、TabContent、双向 Binding、宿主与所有权需要少量语义适配，这些适配不包含业务页面。

运行时路径为 Kotlin 的 `@Composable` 声明 → Compose Runtime 节点更新 → 生成的有类型 Kotlin 调用 → 对应原生入口 → 官方 SwiftUI。没有运行时 JSON 页面或组件树序列化协议。

## 生成和验证

```sh
./gradlew :swiftui-codegen:showWhitelist
./gradlew :swiftui-codegen:generateBindings
```

产物位于 `build/generated/native-ui/`，包括接口源码、选中组件清单、绑定清单和待补能力清单。构建任务自动生成后再编译 Swift、cinterop 和 Kotlin；不要求用户手写或同步跨语言重复代码。

验证应分别覆盖白名单筛选、可重复生成、原生编译、Compose 更新、回调、资源释放、真实窗口、像素和性能。生成器输出或编译成功不等于控件完整行为已经验证，也不能据此声称 100% 像素一致或低资源占用。
