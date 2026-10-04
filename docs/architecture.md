# Architecture

The UI is declared in Kotlin and rendered by SwiftUI. Compose Runtime coordinates state and incremental updates; it does not draw the interface.

| Responsibility | Owner |
| --- | --- |
| Layout, styling, state, callbacks, and application decisions | Kotlin Composables |
| Composition, node identity, and updates | Compose Runtime and the Kotlin adapter |
| Official component and modifier calls, type conversion, and native hosting | The thin Swift bridge |
| Measurement, drawing, and platform control behavior | SwiftUI |

A composition has one native root host. Native controls are children of that tree, so state changes can update controls without replacing the window host.

The language boundary keeps application-specific design out of the native adapter. Adding a screen changes Kotlin UI; adding a missing native capability extends the [binding model](code-generation.md).

SwiftUI's generic views and content builders are adapted through a typed C ABI. Kotlin retains node ownership and callback references; Swift holds the native view tree. Both sides release their resources when the composition and host close.

Module responsibilities are summarized in the [README](../README.md#how-it-works). The [project goal](project-goal.md) describes the remaining capability and performance work.
