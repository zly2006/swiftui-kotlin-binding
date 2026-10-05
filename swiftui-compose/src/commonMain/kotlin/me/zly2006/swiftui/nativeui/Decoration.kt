package me.zly2006.swiftui.nativeui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Composes an arbitrary native background behind this content. */
fun Modifier.background(
    alignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit,
): Modifier = then(BackgroundContentElement(alignment, content))

/** Composes arbitrary native content over this content. */
fun Modifier.overlay(
    alignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit,
): Modifier = then(OverlayContentElement(alignment, content))

/** Uses native content's alpha as a mask for this content. */
fun Modifier.mask(
    alignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit,
): Modifier = then(MaskContentElement(alignment, content))

/** Adds a filled continuous rounded rectangle as a background, using native points. */
fun Modifier.roundedBackground(
    color: NativeColor,
    radius: Double,
): Modifier = background { RoundedRectangle(radius, color) }

/** Overlays a continuous rounded rectangle stroke centered on its boundary. */
fun Modifier.roundedBorder(
    color: NativeColor,
    radius: Double,
    lineWidth: Double = 1.0,
): Modifier = overlay { RoundedRectangleStroke(color, radius, lineWidth) }

/** Overlays a continuous rounded rectangle stroke inset from its boundary. */
fun Modifier.insetRoundedBorder(
    color: NativeColor,
    radius: Double,
    lineWidth: Double = 1.0,
): Modifier = overlay { RoundedRectangleStroke(color, radius, lineWidth, inset = true) }

/** Overlays a circle stroke centered on its boundary. Width uses native points. */
fun Modifier.circleBorder(
    color: NativeColor,
    lineWidth: Double = 1.0,
): Modifier = overlay { CircleOutline(color, lineWidth) }

/** Overlays an inset capsule stroke. Width uses native points. */
fun Modifier.capsuleBorder(
    color: NativeColor,
    lineWidth: Double = 1.0,
): Modifier = overlay { CapsuleOutline(color, lineWidth) }

/** Masks with a rounded rectangle whose dimensions and radius use native points. */
fun Modifier.roundedMask(
    radius: Double,
    width: Double,
    height: Double,
    scale: Double = 1.0,
): Modifier = mask { RoundedRectangleShape(radius, Modifier.scale(scale, scale).frame(width, height)) }

/** Adds a capsule filled with [color] behind the content. */
fun Modifier.capsuleBackground(color: NativeColor): Modifier = background { Capsule(color) }

private data class BackgroundContentElement(
    val alignment: Alignment,
    val background: @Composable () -> Unit,
) : NativeViewModifierElement {
    @Composable override fun Content(content: @Composable () -> Unit) =
        BackgroundContent(
            alignment,
            content = content,
            background = background,
        )
}

private data class OverlayContentElement(
    val alignment: Alignment,
    val overlay: @Composable () -> Unit,
) : NativeViewModifierElement {
    @Composable override fun Content(content: @Composable () -> Unit) = OverlayContent(alignment, content = content, overlay = overlay)
}

private data class MaskContentElement(
    val alignment: Alignment,
    val mask: @Composable () -> Unit,
) : NativeViewModifierElement {
    @Composable override fun Content(content: @Composable () -> Unit) = MaskContent(alignment, content = content, mask = mask)
}
