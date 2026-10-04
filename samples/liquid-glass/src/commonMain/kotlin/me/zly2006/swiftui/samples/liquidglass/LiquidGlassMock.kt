package me.zly2006.swiftui.samples.liquidglass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import me.zly2006.swiftui.nativeui.Alignment
import me.zly2006.swiftui.nativeui.Box
import me.zly2006.swiftui.nativeui.Button
import me.zly2006.swiftui.nativeui.CanvasStroke
import me.zly2006.swiftui.nativeui.Circle
import me.zly2006.swiftui.nativeui.CircleGradientBorder
import me.zly2006.swiftui.nativeui.Column
import me.zly2006.swiftui.nativeui.Ellipse
import me.zly2006.swiftui.nativeui.EllipticalGradientColors
import me.zly2006.swiftui.nativeui.FontWeight
import me.zly2006.swiftui.nativeui.GeometryReader
import me.zly2006.swiftui.nativeui.HorizontalAlignment
import me.zly2006.swiftui.nativeui.Label
import me.zly2006.swiftui.nativeui.LinearGradientColors
import me.zly2006.swiftui.nativeui.LinearGradientPair
import me.zly2006.swiftui.nativeui.LinearGradientStops
import me.zly2006.swiftui.nativeui.MeshGradient
import me.zly2006.swiftui.nativeui.NativeColor
import me.zly2006.swiftui.nativeui.NativeControlSize
import me.zly2006.swiftui.nativeui.NativeModifier
import me.zly2006.swiftui.nativeui.NativePath
import me.zly2006.swiftui.nativeui.NativePathCommand
import me.zly2006.swiftui.nativeui.NativeSize
import me.zly2006.swiftui.nativeui.PathFillGradient
import me.zly2006.swiftui.nativeui.RadialGradientColors
import me.zly2006.swiftui.nativeui.Rectangle
import me.zly2006.swiftui.nativeui.RoundedGradientBorder
import me.zly2006.swiftui.nativeui.RoundedRectangle
import me.zly2006.swiftui.nativeui.Row
import me.zly2006.swiftui.nativeui.SolidColor
import me.zly2006.swiftui.nativeui.Spacer
import me.zly2006.swiftui.nativeui.StyledButton
import me.zly2006.swiftui.nativeui.SystemImage
import me.zly2006.swiftui.nativeui.Text
import me.zly2006.swiftui.nativeui.TextStyle
import me.zly2006.swiftui.nativeui.UnitPoint
import me.zly2006.swiftui.nativeui.background
import me.zly2006.swiftui.nativeui.blur
import me.zly2006.swiftui.nativeui.capsuleBackground
import me.zly2006.swiftui.nativeui.capsuleBorder
import me.zly2006.swiftui.nativeui.circleBorder
import me.zly2006.swiftui.nativeui.clipCircle
import me.zly2006.swiftui.nativeui.clipRounded
import me.zly2006.swiftui.nativeui.clipped
import me.zly2006.swiftui.nativeui.controlSize
import me.zly2006.swiftui.nativeui.fixedVertical
import me.zly2006.swiftui.nativeui.flexibleFrame
import me.zly2006.swiftui.nativeui.font
import me.zly2006.swiftui.nativeui.foreground
import me.zly2006.swiftui.nativeui.frame
import me.zly2006.swiftui.nativeui.hover
import me.zly2006.swiftui.nativeui.ignoreTopSafeArea
import me.zly2006.swiftui.nativeui.insetRoundedBorder
import me.zly2006.swiftui.nativeui.multilineCenter
import me.zly2006.swiftui.nativeui.offset
import me.zly2006.swiftui.nativeui.opacity
import me.zly2006.swiftui.nativeui.padding
import me.zly2006.swiftui.nativeui.rotation
import me.zly2006.swiftui.nativeui.roundedBackground
import me.zly2006.swiftui.nativeui.roundedBorder
import me.zly2006.swiftui.nativeui.roundedMask
import me.zly2006.swiftui.nativeui.scale
import me.zly2006.swiftui.nativeui.semanticFont
import me.zly2006.swiftui.nativeui.shadow
import me.zly2006.swiftui.nativeui.tint
import me.zly2006.swiftui.nativeui.tracking
import me.zly2006.swiftui.nativeui.weight
import kotlin.math.roundToInt

// UI adapted from LiquidGlassDemo by Sohrab Sheikhani, MIT. See LICENSE.
private fun hex(
    value: Int,
    alpha: Double = 1.0,
) = NativeColor.rgba(
    ((value shr 16) and 255) / 255.0,
    ((value shr 8) and 255) / 255.0,
    (value and 255) / 255.0,
    alpha,
)

data class Palette(
    val name: String,
    val background: Int,
    val panel: Int,
    val border: Int,
    val divider: Int,
    val accent: Int,
    val pill: Int,
    val pillBorder: Int,
)

private val palettes =
    listOf(
        Palette("Slate", 0x0B0D12, 0x14171F, 0x262B36, 0x333A47, 0x5E7CE0, 0x29303D, 0x3A4557),
        Palette("Nous", 0x0E1420, 0x161E2E, 0x25304A, 0x2E3B5A, 0x5B8DEF, 0x2E52B8, 0x3C63C9),
        Palette("Midnight", 0x12122A, 0x1B1B38, 0x2E2C55, 0x38356A, 0x9385F0, 0x241F45, 0x3A3568),
        Palette("Ember", 0x1E1210, 0x2A1B16, 0x3E2A22, 0x4A2E24, 0xE0793F, 0x3D1E14, 0x522A1C),
        Palette("Mono", 0x0E0E0E, 0x181818, 0x2A2A2A, 0x333333, 0xAAAAAA, 0x1E1E1E, 0x2E2E2E),
        Palette("Cyberpunk", 0x060A06, 0x0E160E, 0x1C2A1C, 0x24361F, 0x4CE44C, 0x0E2810, 0x1E5020),
    )

class GlassState {
    var theme by mutableStateOf(5)
    var inspector by mutableStateOf(true)
    var settings by mutableStateOf(false)
    var hovered by mutableStateOf(false)
    var fixedHover = false
    var opacity by mutableStateOf(0.8)
    var blur by mutableStateOf(8.0)
    var viewport by mutableStateOf(NativeSize(660.0, 727.0))
}

private val primary = hex(0xDDE1E7)
private val secondary = hex(0x868D98)

private fun mix(
    a: Int,
    b: Int,
    t: Double,
): Int {
    fun channel(shift: Int) = (((a shr shift) and 255) + (((b shr shift) and 255) - ((a shr shift) and 255)) * t).roundToInt() and 255
    return (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
}

@Composable fun LiquidGlassMock(
    state: GlassState,
    dark: Boolean,
) {
    val p = palettes[state.theme]
    Column(spacing = 0.0, modifier = NativeModifier.insetRoundedBorder(hex(p.divider), 10.0).ignoreTopSafeArea()) {
        Box(modifier = NativeModifier.frame(height = 32.0).flexibleFrame(maxWidth = Double.POSITIVE_INFINITY)) {
            SolidColor(hex(p.background))
            Text("Liquid Glass", NativeModifier.font(13.0, FontWeight.Semibold).foreground(secondary))
        }
        Rectangle(hex(p.divider), NativeModifier.frame(height = 1.0))
        Row(spacing = 0.0) {
            Box(alignment = Alignment.Trailing, modifier = NativeModifier.frame(width = 240.0)) {
                SolidColor(hex(p.background).opacity(0.7))
                Column(
                    spacing = 2.0,
                    alignment = HorizontalAlignment.Leading,
                    modifier =
                        NativeModifier
                            .padding(
                                leading = 8.0,
                                bottom = 10.0,
                                trailing = 8.0,
                            ).flexibleFrame(
                                maxWidth = Double.POSITIVE_INFINITY,
                                maxHeight = Double.POSITIVE_INFINITY,
                                alignment = Alignment.TopLeading,
                            ),
                ) {
                    Text(
                        "Themes",
                        NativeModifier.font(11.0).foreground(secondary).padding(top = 12.0, leading = 10.0, bottom = 6.0, trailing = 10.0),
                    )
                    palettes.forEachIndexed { index, swatch ->
                        Button(plain = true, onClick = { state.theme = index }) {
                            Row(
                                spacing = 9.0,
                                modifier =
                                    NativeModifier.padding(top = 6.0, leading = 10.0, bottom = 6.0, trailing = 10.0).roundedBackground(
                                        if (index ==
                                            state.theme
                                        ) {
                                            primary.opacity(0.06)
                                        } else {
                                            NativeColor.Clear
                                        },
                                        7.0,
                                    ),
                            ) {
                                Box(modifier = NativeModifier.frame(width = 12.0, height = 12.0)) {
                                    Circle(hex(swatch.pill), NativeModifier.circleBorder(hex(swatch.pillBorder)))
                                }
                                Text(
                                    swatch.name,
                                    NativeModifier.font(13.0).foreground(
                                        if (index ==
                                            state.theme
                                        ) {
                                            primary
                                        } else {
                                            secondary
                                        },
                                    ),
                                )
                                Spacer()
                                if (index == state.theme) SystemImage("checkmark", NativeModifier.font(11.0).foreground(hex(p.accent)))
                            }
                        }
                    }
                    Spacer(0.0)
                }
                Rectangle(hex(p.divider), NativeModifier.frame(width = 1.0))
            }
            GeometryReader(onSizeChanged = { if (it.width > 0) state.viewport = it }) {
                Box(
                    modifier =
                        NativeModifier
                            .flexibleFrame(
                                maxWidth = Double.POSITIVE_INFINITY,
                                maxHeight = Double.POSITIVE_INFINITY,
                            ).clipped(),
                ) {
                    Backdrop(state, p)
                    Box(
                        modifier = NativeModifier.blur(state.blur).roundedMask(24.0, 440.0, 400.0, if (state.hovered) 1.02 else 1.0),
                    ) { Backdrop(state, p) }
                    RoundedRectangle(
                        24.0,
                        hex(p.panel).opacity(state.opacity),
                        NativeModifier
                            .frame(
                                width = 440.0,
                                height = 400.0,
                            ).scale(
                                if (state.hovered) 1.02 else 1.0,
                                if (state.hovered) 1.02 else 1.0,
                            ).shadow(NativeColor.Black.opacity(0.5), 30.0, y = 14.0),
                    )
                    Box(
                        modifier =
                            NativeModifier
                                .frame(
                                    width = 440.0,
                                    height = 400.0,
                                ).roundedBorder(
                                    hex(p.border),
                                    24.0,
                                ).scale(if (state.hovered) 1.02 else 1.0, if (state.hovered) 1.02 else 1.0)
                                .hover {
                                    if (!state.fixedHover) {
                                        state.hovered =
                                            it
                                    }
                                },
                    ) {
                        HeroContent(state, p)
                        RoundedGradientBorder(
                            listOf(NativeColor.White.opacity(0.28), NativeColor.White.opacity(0.02)),
                            UnitPoint.Top,
                            UnitPoint.Center,
                            24.0,
                            1.0,
                        )
                    }
                }
            }
            if (state.inspector) {
                Box(alignment = Alignment.Leading, modifier = NativeModifier.frame(width = 280.0)) {
                    SolidColor(hex(p.background).opacity(0.7))
                    Column(
                        spacing = 0.0,
                        alignment = HorizontalAlignment.Leading,
                        modifier =
                            NativeModifier
                                .padding(
                                    leading = 8.0,
                                    bottom = 10.0,
                                    trailing = 8.0,
                                ).flexibleFrame(
                                    maxWidth = Double.POSITIVE_INFINITY,
                                    maxHeight = Double.POSITIVE_INFINITY,
                                    alignment = Alignment.TopLeading,
                                ),
                    ) {
                        Text(
                            "Inspector",
                            NativeModifier
                                .font(
                                    11.0,
                                ).foreground(secondary)
                                .padding(top = 12.0, leading = 10.0, bottom = 6.0, trailing = 10.0),
                        )
                        listOf(
                            "Theme" to p.name,
                            "Mode" to "Dark",
                            "Card Opacity" to "${(state.opacity * 100).roundToInt()}%",
                            "Card Blur" to "${(state.blur / 24 * 100).roundToInt()}%",
                        ).forEach { (label, value) ->
                            Column(spacing = 0.0) {
                                Row(modifier = NativeModifier.padding(top = 8.0, leading = 10.0, bottom = 8.0, trailing = 10.0)) {
                                    Text(label, NativeModifier.font(13.0).foreground(secondary))
                                    Spacer()
                                    Text(value, NativeModifier.font(12.0, FontWeight.Medium).foreground(primary))
                                }
                                Rectangle(hex(p.divider), NativeModifier.frame(height = 1.0).padding(leading = 10.0))
                            }
                        }
                        Spacer(0.0)
                    }
                    Rectangle(hex(p.divider), NativeModifier.frame(width = 1.0))
                }
            }
        }
    }
}

@Composable private fun Backdrop(
    state: GlassState,
    p: Palette,
) {
    Box {
        MeshGradient(
            3.0,
            3.0,
            listOf(
                0.0,
                0.0,
                0.5,
                0.0,
                1.0,
                0.0,
                0.0,
                0.5,
                0.5,
                0.5,
                1.0,
                0.5,
                0.0,
                1.0,
                0.5,
                1.0,
                1.0,
                1.0,
            ),
            listOf(0.0, 0.06, 0.0, 0.10, 0.18, 0.08, 0.0, 0.06, 0.02).map {
                hex(mix(p.background, p.accent, it))
            },
        )
        val minor = mutableListOf<NativePathCommand>()
        val major = mutableListOf<NativePathCommand>()

        fun line(
            a: NativePathCommand.Move,
            b: NativePathCommand.Line,
            index: Int,
        ) {
            (
                if (index % 4 ==
                    0
                ) {
                    major
                } else {
                    minor
                }
            ).apply {
                add(a)
                add(b)
            }
        }
        var index = 0
        var x = 0.0
        while (x <=
            state.viewport.width
        ) {
            line(NativePathCommand.Move(x, 0.0), NativePathCommand.Line(x, state.viewport.height), index++)
            x += 40.0
        }
        index = 0
        var y = 0.0
        while (y <=
            state.viewport.height
        ) {
            line(NativePathCommand.Move(0.0, y), NativePathCommand.Line(state.viewport.width, y), index++)
            y += 40.0
        }
        CanvasStroke(NativePath(minor), NativeColor.White.opacity(0.10), 1.0)
        CanvasStroke(NativePath(major), hex(p.accent).opacity(0.25), 1.0)
    }
}

@Composable private fun HeroContent(
    state: GlassState,
    p: Palette,
) {
    Column(spacing = 0.0, modifier = NativeModifier.padding(leading = 36.0, trailing = 36.0)) {
        Text(
            p.name.uppercase(),
            NativeModifier
                .font(
                    11.0,
                    FontWeight.Semibold,
                ).tracking(
                    0.6,
                ).foreground(
                    hex(p.accent),
                ).padding(
                    top = 4.0,
                    leading = 10.0,
                    bottom = 4.0,
                    trailing = 10.0,
                ).capsuleBackground(hex(p.accent).opacity(0.12))
                .capsuleBorder(hex(p.accent).opacity(0.35)),
        )
        AppIcon(
            NativeModifier
                .scale(
                    76.0 / 1024,
                    76.0 / 1024,
                ).frame(width = 76.0, height = 76.0)
                .shadow(hex(p.accent).opacity(0.35), 12.0, y = 6.0)
                .padding(top = 20.0),
        )
        Text(
            "Liquid Glass",
            NativeModifier
                .semanticFont(TextStyle.LargeTitle)
                .weight(FontWeight.Bold)
                .foreground(primary)
                .padding(top = 18.0),
        )
        Text(
            "Real Liquid Glass over a live themed mesh — tint, frost, and light respond as you tune them.",
            NativeModifier
                .semanticFont(TextStyle.Callout)
                .multilineCenter()
                .foreground(secondary)
                .fixedVertical()
                .padding(top = 8.0),
        )
        Row(spacing = 12.0, modifier = NativeModifier.padding(top = 24.0).controlSize(NativeControlSize.Large)) {
            StyledButton(
                true,
                { state.settings = !state.settings },
                NativeModifier.tint(hex(p.accent)),
            ) { Label("Customize…", "slider.horizontal.3") }
            StyledButton(false, {
                state.inspector = !state.inspector
            }) { Label(if (state.inspector) "Hide Inspector" else "Show Inspector", "sidebar.right") }
        }
    }
}

@Composable private fun AppIcon(modifier: NativeModifier) {
    Box(modifier = modifier) {
        Box(modifier = NativeModifier.frame(width = 1024.0, height = 1024.0)) {
            LinearGradientColors(
                listOf(hex(0xC0B4EC), hex(0x9484D8), hex(0x6F5CBD), hex(0x453A8E)),
                UnitPoint.TopLeading,
                UnitPoint.BottomTrailing,
                NativeModifier.clipRounded(228.0),
            )
            EllipticalGradientColors(
                listOf(NativeColor.White.opacity(0.35), NativeColor.Clear),
                0.5,
                0.0,
                0.0,
                0.85,
                NativeModifier.clipRounded(228.0),
            )
            LinearGradientPair(
                NativeColor.Clear,
                NativeColor.Black.opacity(0.28),
                UnitPoint.Center,
                UnitPoint.Bottom,
                NativeModifier.clipRounded(228.0),
            )
            RoundedGradientBorder(
                listOf(NativeColor.White.opacity(0.8), NativeColor.White.opacity(0.12), NativeColor.Clear),
                UnitPoint.Top,
                UnitPoint.Center,
                224.0,
                5.0,
                NativeModifier.blur(1.0).padding(4.0),
            )
        }
        Box(modifier = NativeModifier.frame(width = 700.0, height = 700.0).offset(y = 8.0)) {
            Circle(NativeColor.Black.opacity(0.28), NativeModifier.blur(44.0).offset(y = 42.0))
            RadialGradientColors(
                listOf(NativeColor.White, hex(0xF0F3FF), hex(0xCDD6F6)),
                0.32,
                0.24,
                60.0,
                560.0,
                NativeModifier.clipCircle(),
            )
            LinearGradientStops(
                listOf(NativeColor.Clear, hex(0x6F5CBD).opacity(0.35)),
                listOf(0.55, 1.0),
                modifier = NativeModifier.clipCircle(),
            )
            val path = dropPath()
            PathFillGradient(path, listOf(hex(0x6A58BC), hex(0x3D2C85)), modifier = NativeModifier.frame(width = 400.0, height = 465.0))
            PathFillGradient(
                path,
                listOf(NativeColor.White.opacity(0.35), NativeColor.Clear),
                UnitPoint.Top,
                UnitPoint.Center,
                NativeModifier.frame(width = 400.0, height = 465.0),
            )
            Ellipse(
                NativeColor.White.opacity(0.95),
                NativeModifier
                    .frame(width = 210.0, height = 130.0)
                    .rotation(-24.0)
                    .blur(16.0)
                    .offset(-155.0, -205.0),
            )
            Ellipse(
                NativeColor.White,
                NativeModifier
                    .frame(width = 64.0, height = 40.0)
                    .rotation(-24.0)
                    .blur(8.0)
                    .offset(-215.0, -245.0),
            )
            CircleGradientBorder(listOf(NativeColor.White.opacity(0.9), hex(0x6F5CBD).opacity(0.4)), 4.0, NativeModifier.blur(1.0))
        }
    }
}

private fun dropPath(): NativePath {
    val w = 400.0
    val h = 465.0
    val r = h * 0.42
    return NativePath(
        listOf(
            NativePathCommand.Move(w * 0.5, h * 0.05),
            NativePathCommand.Curve(
                w * 0.5 - r * 0.643,
                h * 0.60 - r * 0.766,
                w * 0.47,
                h * 0.18,
                w * 0.36,
                h * 0.20,
            ),
            NativePathCommand.Arc(w * 0.5, h * 0.60, r, 230.0, 310.0, true),
            NativePathCommand.Curve(w * 0.5, h * 0.05, w * 0.64, h * 0.20, w * 0.53, h * 0.18),
            NativePathCommand.Close,
        ),
    )
}
