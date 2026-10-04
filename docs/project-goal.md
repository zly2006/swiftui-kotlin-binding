# Project goal

Make Apple-native UI available through Kotlin and Compose syntax, with the native platform's rendering behavior and resource advantages.

Kotlin should express application layout, appearance, state, and interaction. Swift remains a thin adapter to official APIs. The language boundary is described in [Architecture](architecture.md).

## Work ahead

- Cover the controls, layouts, effects, animation, accessibility, and lifecycle capabilities needed by real applications. Grow the component selection from official documentation and actual app usage.
- Support Apple platform and architecture differences explicitly. macOS arm64 is the current starting point.
- Validate behavior with real applications, including native input and accessibility, rather than relying on compilation alone.
- Compare CPU, memory, allocation, and frame behavior with equivalent native implementations under idle, scrolling, animation, state updates, and teardown.

The library's full coverage and performance goals remain separate from the capabilities shipped by its early releases.
