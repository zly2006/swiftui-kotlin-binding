package me.zly2006.swiftui.nativeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key

/** Keyed Kotlin composition; the native child list renders with SwiftUI.ForEach. */
@Composable
fun <T> ForEach(
    items: List<T>,
    identifier: (T) -> Any,
    content: @Composable (T) -> Unit,
) {
    items.forEach { item -> key(identifier(item)) { content(item) } }
}
