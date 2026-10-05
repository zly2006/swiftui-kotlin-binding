package me.zly2006.swiftui.nativeui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Renders a native image from exactly one bundle resource, SF Symbol, or filesystem path.
 * [Image](https://developer.apple.com/documentation/swiftui/image) in Apple Documentation
 * @param name Bundle resource name.
 * @param systemName SF Symbol name.
 * @param file Filesystem path for an AppKit image.
 * @param resizable Whether the native image follows the size offered by its container.
 * @param modifier Native-backed Compose modifier chain.
 */
@Composable
fun Image(
    name: String? = null,
    systemName: String? = null,
    file: String? = null,
    resizable: Boolean = false,
    modifier: Modifier = Modifier,
) {
    require(
        (
            if (name !=
                null
            ) {
                1
            } else {
                0
            }
        ) + (
            if (systemName !=
                null
            ) {
                1
            } else {
                0
            }
        ) + (if (file != null) 1 else 0) == 1,
    ) { "Image requires exactly one source: name, systemName, or file" }
    when {
        systemName != null -> SystemImage(systemName, resizable, modifier)
        file != null -> FileImage(file, resizable, modifier)
        else -> ResourceImage(checkNotNull(name), resizable, modifier)
    }
}
