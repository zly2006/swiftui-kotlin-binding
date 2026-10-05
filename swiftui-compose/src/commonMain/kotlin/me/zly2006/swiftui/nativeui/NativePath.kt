package me.zly2006.swiftui.nativeui

/** Immutable path operations; coordinates and radii use native points, and angles use degrees.
 * [Path](https://developer.apple.com/documentation/swiftui/path) in Apple Documentation
 */
sealed interface NativePathCommand {
    /** Starts a subpath at the given point.
     * @property x Horizontal endpoint or center coordinate, in points.
     * @property y Vertical endpoint or center coordinate, in points.
     */
    data class Move(
        val x: Double,
        val y: Double,
    ) : NativePathCommand

    /** Adds a straight line from the current point.
     * @property x Horizontal endpoint or center coordinate, in points.
     * @property y Vertical endpoint or center coordinate, in points.
     */
    data class Line(
        val x: Double,
        val y: Double,
    ) : NativePathCommand

    /** Adds a cubic Bezier curve ending at the given point.
     * @property x Horizontal endpoint or center coordinate, in points.
     * @property y Vertical endpoint or center coordinate, in points.
     * @property c1x Control-point coordinate in points.
     * @property c1y Control-point coordinate in points.
     * @property c2x Control-point coordinate in points.
     * @property c2y Control-point coordinate in points.
     */
    data class Curve(
        val x: Double,
        val y: Double,
        val c1x: Double,
        val c1y: Double,
        val c2x: Double,
        val c2y: Double,
    ) : NativePathCommand

    /** Adds a circular arc around a center point.
     * @property x Horizontal endpoint or center coordinate, in points.
     * @property y Vertical endpoint or center coordinate, in points.
     * @property radius Arc radius in points.
     * @property start Start angle in degrees.
     * @property end End angle in degrees.
     * @property clockwise Whether the arc follows the clockwise direction.
     */
    data class Arc(
        val x: Double,
        val y: Double,
        val radius: Double,
        val start: Double,
        val end: Double,
        val clockwise: Boolean,
    ) : NativePathCommand

    /** Closes the current subpath by connecting it to its starting point. */
    data object Close : NativePathCommand
}

/** Immutable geometry with a precomputed structural hash for native resource reuse.
 * @param commands Operations to snapshot; later mutations of the input list cannot change this path.
 */
class NativePath(
    commands: List<NativePathCommand>,
) {
    private class Commands(
        private val values: Array<NativePathCommand>,
    ) : AbstractList<NativePathCommand>() {
        override val size get() = values.size

        override fun get(index: Int) = values[index]
    }

    /** Read-only snapshot of the path operations. */
    val commands: List<NativePathCommand> = Commands(commands.toTypedArray())
    private val structuralHash = this.commands.hashCode()

    /** Returns the cached structural hash of the immutable operations. */
    override fun hashCode() = structuralHash

    /** Compares the immutable command sequence, with a cached-hash fast check. */
    override fun equals(other: Any?) =
        this === other || other is NativePath && structuralHash == other.structuralHash && commands == other.commands

    /** Returns a diagnostic representation of the operations. */
    override fun toString() = "NativePath(commands=$commands)"

    /** Returns the immutable operation sequence for destructuring. */
    operator fun component1() = commands

    /** Creates a new immutable snapshot of [commands], defaulting to this path's operations. */
    fun copy(commands: List<NativePathCommand> = this.commands) = NativePath(commands)
}
