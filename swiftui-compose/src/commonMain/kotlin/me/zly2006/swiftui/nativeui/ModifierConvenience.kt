package me.zly2006.swiftui.nativeui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Adds equal padding on all four edges, measured in native points.
 * @param all Inset on every edge, in points.
 * [View.padding](https://developer.apple.com/documentation/swiftui/view) in Apple Documentation
 */
fun Modifier.padding(all: Double): Modifier = padding(top = all, leading = all, bottom = all, trailing = all)

/**
 * Uses SwiftUI's system-selected default padding, rather than a fixed inset.
 * [View.padding](https://developer.apple.com/documentation/swiftui/view) in Apple Documentation
 */
fun Modifier.defaultPadding(): Modifier = then(DefaultInsetsElement)

/**
 * Applies the native tertiary hierarchical foreground style, inheriting the base style from the environment.
 * [HierarchicalShapeStyle.tertiary](https://developer.apple.com/documentation/swiftui/hierarchicalshapestyle/tertiary) in Apple Documentation
 */
fun Modifier.tertiaryForeground(): Modifier = then(TertiaryStyleElement)

private data object DefaultInsetsElement : NativeViewModifierElement {
    @Composable override fun Content(content: @Composable () -> Unit) = DefaultPadding(content = content)
}

private data object TertiaryStyleElement : NativeViewModifierElement {
    @Composable override fun Content(content: @Composable () -> Unit) = TertiaryForeground(content = content)
}

/** Centers all lines in a native text view. */
fun Modifier.multilineCenter(): Modifier = multilineAlignment(NativeTextAlignment.Center)

/** Uses the child's ideal vertical size while preserving horizontal constraints. */
fun Modifier.fixedVertical(): Modifier = fixedSize(horizontal = false, vertical = true)

/** Extends native content through the top safe-area inset. */
fun Modifier.ignoreTopSafeArea(): Modifier = ignoreSafeArea(NativeSafeAreaEdges.Top)

/** Uses AppKit's adaptive window color as the SwiftUI window container background. */
fun Modifier.windowBackground(): Modifier = then(WindowColorElement)

private data object WindowColorElement : NativeViewModifierElement {
    @Composable override fun Content(content: @Composable () -> Unit) = WindowBackground(content = content)
}
