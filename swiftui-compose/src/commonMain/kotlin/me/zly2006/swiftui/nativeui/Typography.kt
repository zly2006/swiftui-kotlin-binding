package me.zly2006.swiftui.nativeui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Applies a native semantic text style, preserving SwiftUI's platform font behavior.
 *
 * @param style Semantic role such as [Font.TextStyle.body] or [Font.TextStyle.headline].
 * [Font](https://developer.apple.com/documentation/swiftui/font) in Apple Documentation
 * [View.font](https://developer.apple.com/documentation/swiftui/view) in Apple Documentation
 */
fun Modifier.font(style: Font.TextStyle): Modifier = then(SemanticTextStyleElement(style))

private data class SemanticTextStyleElement(
    val style: Font.TextStyle,
) : NativeViewModifierElement {
    @Composable override fun Content(content: @Composable () -> Unit) {
        SemanticFont(style, content = content)
    }
}

/**
 * Applies SwiftUI's second caption text style with the requested weight.
 * @param weight Native font weight applied to the caption font.
 * [Font.caption2](https://developer.apple.com/documentation/swiftui/font/caption2) in Apple Documentation
 */
fun Modifier.caption2(weight: Font.Weight = Font.Weight.regular): Modifier = then(Modifier.fontWeight(weight).font(Font.TextStyle.caption2))
