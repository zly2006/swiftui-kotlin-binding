# 关键原生组件白名单

白名单先覆盖常用 UI 的组件、容器和必要效果，再用真实应用补齐遗漏。它不是 SDK 符号清单，不要求导出每个函数、构造重载、协议、辅助类型或系统扩展。完整 UI 能力通过必要的原生调用与组合实现。

可执行白名单位于 `swiftui-codegen/src/main/kotlin/me/zly2006/swiftui/generator/Whitelist.kt`。语义适配定义位于同目录的 `Model.kt`。只有同时列入白名单且有适配定义的绑定才能生成；仅添加适配定义不会自动导出。内部根节点属于运行时，不计为用户控件。

## 第一批关键组件

| 类别 | 关键组件和能力 | 当前状态 |
| --- | --- | --- |
| 文字和图片 | Text、Image/SF Symbols；Label、图片资源、AsyncImage | Text 与系统符号已生成，其余待补。 |
| 布局 | HStack、VStack、ZStack、Group、Spacer、Divider、GeometryReader；Grid 和 LazyGrid | 栈、分隔和尺寸回调已生成，网格待补。 |
| 滚动与集合 | ScrollView、LazyHStack、LazyVStack；ScrollViewReader、List、Section、Form、Table | 滚动与懒栈已生成，集合和滚动定位待补。 |
| 操作和输入 | Button、Toggle、TextField、Slider、ProgressView；Picker、Stepper、ColorPicker、DatePicker、SecureField、TextEditor | 前五类已生成，其他进入下一批。旧实验中的 ColorPicker 尚未迁入新的原生运行时。 |
| 导航 | NavigationStack、TabView、Tab、TabSection；NavigationLink、NavigationSplitView、toolbar | 栈、侧栏式标签和隐藏工具栏已生成，其余待补。 |
| 弹窗和菜单 | sheet、popover、alert、confirmationDialog、Menu、contextMenu、文件选择 | 查漏后列入待实现。 |
| 视觉效果 | 原生颜色、形状、渐变、材质、字体、前景、裁切、阴影、透明度、缩放 | 已生成基本组合；原生动画、转场、符号动效、玻璃效果与环境传播仍需补齐。 |
| 输入与无障碍 | disabled、accessibilityLabel；focus、submit、search、手势、快捷键、生命周期 | 禁用与标签已生成；其他行为列为待实现。 |

“已生成”只表示生成器已有相应转发代码。Swift 和 Kotlin 编译、真实控件行为、像素及性能分别验证，不能从表中的数量推断目标完成。

## 官方文档依据

组件起点来自 Apple 的 [Controls and indicators](https://developer.apple.com/documentation/swiftui/controls-and-indicators)、[Layout fundamentals](https://developer.apple.com/documentation/swiftui/layout-fundamentals)、[Text input and output](https://developer.apple.com/documentation/swiftui/text-input-and-output)、[Scroll views](https://developer.apple.com/documentation/swiftui/scroll-views) 和 [NavigationStack](https://developer.apple.com/documentation/swiftui/navigationstack)。样式与效果按相应组件和 [View](https://developer.apple.com/documentation/swiftui/view) 的必要配置接口补充。

只查看白名单需要的声明、重载和 availability，并通过 Swift 编译器核对所选调用。生成流程没有全量 symbol graph 导出或全 SDK 审计任务。

## 开源项目查漏

以下源码在 2026-10-04 已核读，用于判断真实 UI 缺口，不复制其业务代码到绑定库：

| 项目与固定源码 | 发现的需求 | 对白名单的补充 |
| --- | --- | --- |
| [CodeEdit GeneralSettingsView](https://github.com/CodeEditApp/CodeEdit/blob/fa2aebd86373211c78626074b53ab75010767575/CodeEdit/Features/Settings/Pages/GeneralSettings/GeneralSettingsView.swift) | Section、Picker、Toggle、多行 TextField、LabeledContent | 补充 Form/Section、Picker、LabeledContent 与输入重载。 |
| [CodeEdit TextEditingSettingsView](https://github.com/CodeEditApp/CodeEdit/blob/fa2aebd86373211c78626074b53ab75010767575/CodeEdit/Features/Settings/Pages/TextEditingSettings/TextEditingSettingsView.swift) | Stepper、Picker、sheet | 补充 Stepper 和受控弹窗。 |
| [IceCubes DisplaySettingsView](https://github.com/Dimillian/IceCubesApp/blob/9efcb16e720f337a401cf61c8e300dd043368282/IceCubesApp/App/Tabs/Settings/DisplaySettingsView.swift) | Form、Section、ColorPicker、Picker、Slider、NavigationLink | 补充表单、颜色选择、导航跳转和带步长的滑块。 |

本轮核读的是这些代表性页面，不是对两个开源项目的完整扫描。后续遇到新的页面或效果，先登记实际需求、官方 API 和源码依据，再选择需要生成的接口。

## 扩展规则

新增能力时，先确认已有原生组件和组合是否足够；只补充缺失的必要接口。白名单要写出官方文档、应用用例、适配策略和验证状态。生成器不能把偶然发现的符号自动变成公共 API。ABI、Kotlin 和 Swift 的重复代码由同一模型生成，复杂 Binding、子内容和原生宿主只保留必要语义适配。
