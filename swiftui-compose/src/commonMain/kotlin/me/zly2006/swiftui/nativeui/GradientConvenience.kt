package me.zly2006.swiftui.nativeui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Creates a native linear gradient from two colors.
 * [LinearGradient](https://developer.apple.com/documentation/swiftui/lineargradient) in Apple Documentation
 */
@Composable
fun LinearGradientPair(
    first: NativeColor,
    last: NativeColor,
    start: UnitPoint = UnitPoint.Top,
    end: UnitPoint = UnitPoint.Bottom,
    modifier: Modifier = Modifier,
) = LinearGradientColors(listOf(first, last), start, end, modifier)

/**
 * Creates a native linear gradient with an evenly spaced middle color.
 * [LinearGradient](https://developer.apple.com/documentation/swiftui/lineargradient) in Apple Documentation
 */
@Composable
fun LinearGradient(
    first: NativeColor,
    middle: NativeColor,
    last: NativeColor,
    start: UnitPoint = UnitPoint.Top,
    end: UnitPoint = UnitPoint.Bottom,
    modifier: Modifier = Modifier,
) = LinearGradientColors(listOf(first, middle, last), start, end, modifier)
