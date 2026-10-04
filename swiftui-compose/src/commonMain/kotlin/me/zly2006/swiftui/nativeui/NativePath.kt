package me.zly2006.swiftui.nativeui

sealed interface NativePathCommand {
    data class Move(
        val x: Double,
        val y: Double,
    ) : NativePathCommand

    data class Line(
        val x: Double,
        val y: Double,
    ) : NativePathCommand

    data class Curve(
        val x: Double,
        val y: Double,
        val c1x: Double,
        val c1y: Double,
        val c2x: Double,
        val c2y: Double,
    ) : NativePathCommand

    data class Arc(
        val x: Double,
        val y: Double,
        val radius: Double,
        val start: Double,
        val end: Double,
        val clockwise: Boolean,
    ) : NativePathCommand

    data object Close : NativePathCommand
}

data class NativePath(
    val commands: List<NativePathCommand>,
)
