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

/** Immutable geometry with a precomputed structural hash for native resource reuse. */
class NativePath(
    commands: List<NativePathCommand>,
) {
    private class Commands(
        private val values: Array<NativePathCommand>,
    ) : AbstractList<NativePathCommand>() {
        override val size get() = values.size

        override fun get(index: Int) = values[index]
    }

    val commands: List<NativePathCommand> = Commands(commands.toTypedArray())
    private val structuralHash = this.commands.hashCode()

    override fun hashCode() = structuralHash

    override fun equals(other: Any?) =
        this === other || other is NativePath && structuralHash == other.structuralHash && commands == other.commands

    override fun toString() = "NativePath(commands=$commands)"

    operator fun component1() = commands

    fun copy(commands: List<NativePathCommand> = this.commands) = NativePath(commands)
}
