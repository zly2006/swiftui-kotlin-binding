package me.zly2006.swiftui.nativeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.materialize

/** A standard Compose element whose effect is forwarded to an official native API. */
internal interface NativeViewModifierElement : Modifier.Element {
    @Composable fun Content(content: @Composable () -> Unit)
}

/** Unsupported elements fail during composition, before the affected native subtree is created.
 * @property elementType Diagnostic Compose element class name.
 * @property replacement Suggested native API and usage, or null when no specific alternative is known.
 */
class UnsupportedComposeModifierException internal constructor(
    val elementType: String,
    val replacement: String?,
) : IllegalArgumentException(
        buildString {
            append("Compose modifier '$elementType' cannot execute in the SwiftUI native tree. ")
            if (replacement != null) {
                append(replacement)
            } else {
                append("Use supported extensions imported from me.zly2006.swiftui.nativeui. ")
                append("Custom modifiers can use Modifier.composed { ... } to combine those extensions; ")
                append("arbitrary Compose layout, drawing, and input nodes have no automatic native conversion.")
            }
        },
    )

@Composable
internal fun Modifier.renderNative(content: @Composable () -> Unit) {
    if (this === Modifier) {
        content()
        return
    }
    // materialize keeps its Composer groups balanced when a chain gains or loses composed factories.
    val materialized = currentComposer.materialize(this)
    val elements =
        remember(materialized) {
            materialized.foldIn(mutableListOf<NativeViewModifierElement>()) { result, element ->
                if (element !is NativeViewModifierElement) {
                    val type = element::class.simpleName ?: "anonymous Modifier.Element"
                    throw UnsupportedComposeModifierException(type, replacementFor(type))
                }
                result.apply { add(element) }
            }
        }
    RenderElements(elements, 0, content)
}

/** Compose order: the first element wraps the elements that follow it. */
@Composable
private fun RenderElements(
    elements: List<NativeViewModifierElement>,
    index: Int,
    content: @Composable () -> Unit,
) {
    if (index == elements.size) {
        content()
    } else {
        elements[index].Content { RenderElements(elements, index + 1, content) }
    }
}

private fun replacementFor(type: String): String? =
    when (type) {
        "PaddingElement", "PaddingValuesElement" ->
            "Import me.zly2006.swiftui.nativeui.padding and use Modifier.padding(all = 12.0), " +
                "or padding(top = ..., leading = ..., bottom = ..., trailing = ...). Native distances are in points."
        "SizeElement", "UnspecifiedConstraintsElement" ->
            "Import me.zly2006.swiftui.nativeui.frame or flexibleFrame. Use Modifier.frame(width = 120.0, height = 40.0), " +
                "or flexibleFrame(minWidth = ..., maxWidth = ...). SwiftUI frame semantics differ from Compose size/requiredSize."
        "FillElement" ->
            "Import me.zly2006.swiftui.nativeui.flexibleFrame and use Modifier.flexibleFrame(maxWidth = Double.POSITIVE_INFINITY) " +
                "and/or maxHeight = Double.POSITIVE_INFINITY. Fractional fill has no automatic equivalent."
        "WrapContentElement" ->
            "Native views normally use their ideal size. For vertical text sizing, import me.zly2006.swiftui.nativeui.fixedVertical " +
                "and use Modifier.fixedVertical(); for a fixed container use frame(...). Compose unbounded sizing is not equivalent."
        "OffsetElement", "OffsetPxElement" ->
            "Import me.zly2006.swiftui.nativeui.offset and use Modifier.offset(x = 8.0, y = 4.0). " +
                "Values are point offsets; convert pixel values using the display scale. Check right-to-left behavior separately."
        "BackgroundElement" ->
            "Import me.zly2006.swiftui.nativeui.background and NativeColor; use Modifier.background(NativeColor.Red). " +
                "For rounded shapes use roundedBackground(color = ..., radius = ...)."
        "BorderModifierNodeElement" ->
            "Import me.zly2006.swiftui.nativeui.roundedBorder and NativeColor; " +
                "use Modifier.roundedBorder(NativeColor.Red, radius = 8.0, lineWidth = 1.0). " +
                "Use circleBorder(...) for circular borders."
        "BlockGraphicsLayerElement", "GraphicsLayerElement" ->
            "Import opacity, scale, rotation, clipRounded, clipCircle, blur, or shadow from me.zly2006.swiftui.nativeui. " +
                "For example, Modifier.opacity(0.5).clipRounded(8.0). Arbitrary graphicsLayer blocks cannot be translated."
        "ClickableElement", "CombinedClickableElement" ->
            "Use me.zly2006.swiftui.nativeui.Button(plain = true, onClick = { ... }) { ... } " +
                "to make native content clickable. Double-click and long-press behavior have no direct replacement."
        "HoverableElement" ->
            "Import me.zly2006.swiftui.nativeui.hover and use Modifier.hover { hovering -> ... }. " +
                "Compose InteractionSource is not mapped automatically."
        "SemanticsModifier", "AppendedSemanticsElement", "ClearAndSetSemanticsElement" ->
            "Import me.zly2006.swiftui.nativeui.accessibilityLabel and use Modifier.accessibilityLabel(\"Description\"). " +
                "This replaces an accessible label only; test tags and other semantics properties require explicit native bindings."
        "OnSizeChangedModifier" ->
            "Use me.zly2006.swiftui.nativeui.GeometryReader(onSizeChanged = { size -> ... }) { ... }. " +
                "NativeSize uses points rather than pixels. GeometryReader participates in layout, so constrain its frame when necessary."
        "AspectRatioElement" ->
            "Use me.zly2006.swiftui.nativeui.frame with an explicit width and height, e.g. Modifier.frame(width = 160.0, height = 90.0). " +
                "An adaptive aspect-ratio modifier is not implemented yet."
        "DrawBehindElement", "DrawWithContentElement", "DrawWithCacheElement" ->
            "For backgrounds use native background(...); for shapes use Rectangle, Circle, or PathStroke " +
                "from me.zly2006.swiftui.nativeui. Arbitrary DrawScope code has no automatic native equivalent."
        else -> null
    }
