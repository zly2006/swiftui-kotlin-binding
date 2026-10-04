# 维护约束

- 项目是通用库 `swiftui-kotlin-binding`，包名前缀为 `me.zly2006`。外部应用只能作为独立测试案例，不得成为库的依赖。
- 最终目标与验收以 `docs/project-goal.md` 为准。少量控件、示例窗口或单页复刻不能作为完成依据。
- Kotlin、Swift 和模块的责任边界以 `docs/architecture.md` 的“语言责任边界”和“模块依赖边界”为准；修改边界必须先更新该文档，再调整实现。
- 当前模块清单及依赖以 `docs/modules.md` 为准；不得恢复已经停用的 Skia 混合宿主或重复手写组件桥。
- 所有模块、所有 Kotlin source set（含测试和自定义源码目录）及 Gradle Kotlin 脚本必须纳入 ktlint；仅排除 `docs/code-generation.md` 指定的 codegen 产物，生成器自身源码不得排除。格式规则以 `.editorconfig` 为准；`componentWhitelist`、`Model.kt` 和 `MacosNativeRuntime.kt` 的声明正文，以及 generator 包内格式化后行数超过原来 1.5 倍的区块必须保留 `@formatter:off/on`，这些区段禁止自动格式化和检查。提交前必须运行 `./gradlew ktlintFormat`，检查格式化修改，再运行 `./gradlew ktlintCheck` 并确认通过。
- UI 声明、布局意图、视觉参数、状态、行为和 Compose 接入必须由 Kotlin 实现。组件实际绘制使用官方原生 API；禁止手写外观复刻。
- Swift 只允许绑定官方 SwiftUI 能力以及实现必要的宿主、ABI 转换和资源管理；禁止编写应用页面、业务状态、布局算法、设计参数或自绘组件。
- 重复绑定必须按 `docs/code-generation.md` 从同一有类型模型生成。每项原生操作对应强类型 Kotlin API 和独立接口；禁止运行时 JSON 页面、组件树传输协议、字符串命令解释器和 Swift 页面渲染器。句柄必须在主线程更新和释放。
- 覆盖范围必须从关键组件白名单出发，用官方文档和开源应用实际用法补齐；禁止为了建立清单全量导出 SDK 符号，也不得以符号总数代替 UI 能力覆盖。此前把完整 UI 能力误当成全量 API 导出，产生了无关扩展与辅助符号，增加内存、磁盘和验证成本；后续只提取白名单绑定需要的声明，优先完成真实调用链。白名单及依据见 `docs/component-whitelist.md`。
- 纯 Kotlin UI 模块不得导入 Apple 平台包、cinterop 或原生句柄类型。通用绑定模块不得引用测试 UI。
- 未获提交授权的外部页面代码、资源和参考截图只用于本地测试，分别保留在被忽略的 `local-fixtures/` 和 `evidence/`。用户明确授权的 MIT 示例可按 `docs/modules.md` 提交独立 Kotlin 改写、许可证、credits 与实际运行截图；上游源码和本地对照材料仍不得提交。提交必须手动选择文件并逐项检查暂存区，禁止 `git add -A`。
- 原生路径的验证和未完成项以 `docs/native-verification.md` 为准。像素一致性只能按最终产物与参考应用的真实截图报告，不能沿用已废弃实现的结果。
- 测试窗口不得激活应用或抢前台；截图仅采集本任务创建的窗口。
- 渲染路径采用原生按需更新；性能结论必须经过等价原生 UI 对照，不得由技术栈或构建成功推断低占用。

- Maven 发布与原生分发遵循 `docs/releasing.md`。只发布三个库模块；正式上传前必须通过独立消费项目的构建、真实窗口和资源释放检查，最终以公开仓库下载后再次构建运行为准。发布凭据与签名私钥不得进入 Git。
