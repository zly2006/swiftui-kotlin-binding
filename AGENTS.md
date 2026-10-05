# 维护约束

- 通用库目标以 `docs/project-goal.md` 为准。外部 UI 示例不能替代完整能力和性能目标。
- Kotlin 与 Swift 的责任边界以 `docs/architecture.md` 为准；Swift 只适配官方原生能力，不承担应用页面、业务状态或设计参数。
- 新能力按 `docs/code-generation.md` 扩展，先核对官方文档与实际 UI 需求。避免全量导出 SDK，也不要重复手写可由模型生成的绑定。
- 纯 Kotlin UI 不得依赖 Apple 平台包或原生句柄。库模块不得依赖应用示例。
- 提交前运行 `./gradlew ktlintFormat`，检查修改，再运行 `./gradlew ktlintCheck`。格式以 `.editorconfig` 为准，保留现有 `@formatter:off/on` 区域。
- 提交必须手动选择文件，禁止 `git add -A`。独立消费验收代码、上游参考源码与构建缓存留在本地；获授权的开源示例保留许可证和 credits。
- 测试只采集本任务窗口，不激活应用或抢前台。
- 发布范围与坐标以 `build.gradle.kts` 为准。只有用户明确要求发布时才上传；凭据与签名私钥不得进入 Git。
- 性能结论必须有等价原生实现的实测对照。不要从技术栈、生成结果或构建成功推断低占用或完整覆盖。

- 公开运行库 API 必须有 KDoc。生成文档引用官方 API；手写便捷组合留在 Kotlin。文档完整性由 `swiftui-codegen/src/test/kotlin/me/zly2006/swiftui/generator/PublicApiDocumentationTest.kt` 检查。

- 公开 API 命名以 Apple 官方类型和成员为准，具体边界见 `docs/architecture.md`；修改命名时检查完整公开面，不能只修用户举出的例子。Compose Runtime 的复用不意味着控件和类型应借用 Compose UI 名称。
