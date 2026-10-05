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
fun Modifier.padding(): Modifier = then(DefaultInsetsElement)

private data object DefaultInsetsElement : NativeViewModifierElement {
    @Composable override fun Content(content: @Composable () -> Unit) = DefaultPadding(content = content)
}

/** Uses AppKit's adaptive window color as the SwiftUI window container background. */
fun Modifier.windowBackground(): Modifier = then(WindowColorElement)

private data object WindowColorElement : NativeViewModifierElement {
    @Composable override fun Content(content: @Composable () -> Unit) = WindowBackground(content = content)
}
