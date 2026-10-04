# Extending the bindings

SwiftUI's generic views and content builders require adaptation for Kotlin/Native. The generator keeps the C ABI, Swift forwarding, Kotlin backend, and Composable declarations in sync.

The maintained inputs are [Model.kt](../swiftui-codegen/src/main/kotlin/me/zly2006/swiftui/generator/Model.kt), [Whitelist.kt](../swiftui-codegen/src/main/kotlin/me/zly2006/swiftui/generator/Whitelist.kt), and [Emitter.kt](../swiftui-codegen/src/main/kotlin/me/zly2006/swiftui/generator/Emitter.kt). The whitelist also records planned capabilities; it is the component inventory.

## Add a capability

1. Find the required official API and check its platform availability. Start with a real UI use case.
2. Add its typed fields, child content, callback, and native mapping to the semantic model, then select it in the whitelist.
3. Generate and compile the bindings. Keep application layouts and design parameters in Kotlin.
4. Run the relevant native example to check the control, updates, and lifecycle behavior.

```sh
./gradlew :swiftui-codegen:showWhitelist
./gradlew :swiftui-codegen:generateBindings
```

Generated files live in `build/generated/native-ui/`. Edit the maintained model and emitter instead of these outputs. SwiftUI features that do not fit the existing value, callback, or child-content abstractions need a deliberate semantic adapter.
