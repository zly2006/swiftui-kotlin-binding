@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package me.zly2006.swiftui.nativeui

import kotlinx.cinterop.COpaquePointer
import me.zly2006.swiftui.capi.sui_path_arc
import me.zly2006.swiftui.capi.sui_path_close
import me.zly2006.swiftui.capi.sui_path_create
import me.zly2006.swiftui.capi.sui_path_curve
import me.zly2006.swiftui.capi.sui_path_line
import me.zly2006.swiftui.capi.sui_path_move
import me.zly2006.swiftui.capi.sui_path_release

internal fun buildNativePath(spec: NativePath): COpaquePointer {
    checkNativeUiMainThread()
    val path = checkNotNull(sui_path_create())
    try {
        for (command in spec.commands) {
            when (command) {
                is NativePathCommand.Move -> sui_path_move(path, command.x, command.y)
                is NativePathCommand.Line -> sui_path_line(path, command.x, command.y)
                is NativePathCommand.Curve -> sui_path_curve(path, command.x, command.y, command.c1x, command.c1y, command.c2x, command.c2y)
                is NativePathCommand.Arc ->
                    sui_path_arc(
                        path,
                        command.x,
                        command.y,
                        command.radius,
                        command.start,
                        command.end,
                        if (command.clockwise) 1 else 0,
                    )
                NativePathCommand.Close -> sui_path_close(path)
            }
        }
        return path
    } catch (failure: Throwable) {
        sui_path_release(path)
        throw failure
    }
}
