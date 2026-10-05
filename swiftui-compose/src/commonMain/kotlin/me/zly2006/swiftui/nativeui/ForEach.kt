package me.zly2006.swiftui.nativeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key

/** Keyed Kotlin composition; the native child list renders with SwiftUI.ForEach.
 * @param items Ordered data to render.
 * @param identifier Stable, distinct identity for each item; do not use its changing list position.
 * @param content Composable content for an item.
 * [ForEach](https://developer.apple.com/documentation/swiftui/foreach) in Apple Documentation
 */
@Composable
fun <T> ForEach(
    items: List<T>,
    identifier: (T) -> Any,
    content: @Composable (T) -> Unit,
) {
    items.forEach { item -> key(identifier(item)) { content(item) } }
}
