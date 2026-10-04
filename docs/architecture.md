# 原生 UI 库的架构边界

项目目标以 [project-goal.md](project-goal.md) 为准。库通过 Kotlin 和 Compose 的声明式组合机制调用官方原生 UI；外部应用只用于独立测试。本文描述目标架构，当前实现状态单独列在最后。

## 语言责任边界

| 工作 | 责任归属 |
| --- | --- |
| 页面声明、组合结构、子内容、布局意图和参数 | Kotlin 的原生 Composable API。 |
| 颜色、字体、材质、圆角、主题、动画选择和行为策略 | Kotlin 声明，通过有类型的参数调用相应原生能力。 |
| 数据、应用状态、事件决策、业务逻辑和导航意图 | Kotlin。原生事件回传后由 Kotlin 决定状态变化。 |
| 状态观察、重组、节点身份与增量更新 | Compose Runtime 和 Kotlin 绑定运行时。 |
| 官方组件、容器、modifier、style、environment 和原生动画的实际调用 | 生成的 Swift 薄层，逐项调用官方 API。 |
| 官方组件的测量、绘制、系统内部交互、焦点与无障碍 | 原生框架执行，Kotlin 通过对应 API 配置和接收事件。 |
| 类型转换、C ABI、必要的根宿主和原生对象持有 | 最小 Swift 互操作层；Kotlin 管理绑定所有权与主线程调度。 |

Kotlin 声明布局关系及其参数，官方布局容器执行这些参数对应的原生布局语义。Swift 不得另写页面布局算法、选择设计参数或根据产品数据组合页面。官方 modifier 对视图的包装属于 API 转发，不能借此藏入应用视觉策略。

Swift 不得实现应用页面、业务状态、数据源、网络逻辑、自绘控件或产品特例。不得在 Swift 中模仿 Text、Button 等已有官方控件的外观；调用官方控件的薄层由生成器产生。配置默认值与交互策略放在 Kotlin，跨语言回调必须有类型和明确所有权。

Text、容器和 Button 等原生能力也属于覆盖范围，不能因 Compose/Skia 已有同名组件而排除。它们需要绑定官方 API，而不是手写视觉复刻。Compose Runtime 与编译器负责组合机制；直接使用 `androidx.compose.foundation` 的现有绘制组件并不会自动变成 SwiftUI。两种渲染路径的 API 与兼容范围必须公开区分，不能宣称未实现的无修改兼容性。

## 模块依赖边界

当前模块名称和构建依赖以 [modules.md](modules.md) 为准，按以下职责拆分：

| 模块职责 | 允许依赖 |
| --- | --- |
| 白名单、语义模型与代码生成 | 官方文档、关键组件白名单与所选 SDK 调用的映射规则，不依赖应用 UI。 |
| 底层原生绑定 | 生成的逐项接口、值类型、回调和所有权运行时，不依赖应用 UI 或 Compose 绘制实现。 |
| 原生 Compose 接入 | Compose Runtime、生成的原生 API 与节点协调机制，不依赖应用 UI；默认路径不引入 Skia 绘制宿主。 |
| 纯 Kotlin 应用 UI | 原生 Composable 公共 API 和 Kotlin 状态，禁止导入 Apple 平台包、C 指针或 Swift 业务代码。 |
| 平台应用宿主 | 根窗口、原生链接与打包、线程与应用生命周期。 |
| 测试与性能案例 | 消费公共库 API，包含外部 UI mock 和等价原生基线，不进入库的依赖。 |

ABI、Kotlin 与 Swift 绑定来自同一份有类型的语义模型，详见 [code-generation.md](code-generation.md)。运行时每项操作都调用明确的入口，不传 JSON 页面、不发送字符串命令，也不将组件树序列化后交给 Swift 页面解释器。

原生容器和子内容槽需要保留视图身份、环境传播、焦点与原生更新机制。Kotlin 管理组合变化，通过对应的有类型操作更新原生结构；Swift 只负责官方 API 所需的最小容器适配。不能把每个叶子组件独立放进一个 NSHostingView 当成最终架构。

## 性能边界

默认宿主采用原生按需渲染。Compose 状态调度只处理实际变化，不持续轮询、驱动空帧或重建整页。原生动画由 Kotlin 配置后交给官方框架执行，避免逐帧跨语言同步。

静止与后台负载、更新范围、资源持有、回调和卸载必须验证。低占用结论以 [project-goal.md](project-goal.md) 的等价原生对照要求为准；生成器覆盖率、原生链接成功或 Kotlin/Native 编译通过都不能代替性能证据。

## 当前实现状态

旧的 ComposeScene/SkiaLayer 混合实验、配套示例、四类手写组件绑定与过期构建入口已经删除。当前只保留白名单生成和原生 SwiftUI 路径，验证记录见 [native-verification.md](native-verification.md)。

白名单生成器和单根原生 Compose 运行时已有代码，所选 Swift 调用及公共 Kotlin API 已通过编译。白名单与查漏记录见 [component-whitelist.md](component-whitelist.md)。完整能力覆盖、独立消费项目、正式发布、外部 UI 的完整迁移和性能对照仍未完成。后续扩展只处理白名单所需接口，并以真实控件、回调、视图身份和原生对照检验调用链。
