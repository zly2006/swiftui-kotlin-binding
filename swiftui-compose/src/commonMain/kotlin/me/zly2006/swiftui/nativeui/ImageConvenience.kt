package me.zly2006.swiftui.nativeui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Renders an SF Symbol at its native intrinsic size. */
@Composable
fun SystemImage(
    name: String,
    modifier: Modifier = Modifier,
) = SystemImage(name, resizable = false, modifier)

/** Renders an SF Symbol that resizes to the space provided by its native container. */
@Composable
fun SystemResizableImage(
    name: String,
    modifier: Modifier = Modifier,
) = SystemImage(name, resizable = true, modifier)
