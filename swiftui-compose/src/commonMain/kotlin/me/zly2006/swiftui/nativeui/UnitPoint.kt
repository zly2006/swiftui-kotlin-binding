package me.zly2006.swiftui.nativeui

/** Normalized coordinates in a native view's coordinate space.
 * [UnitPoint](https://developer.apple.com/documentation/swiftui/unitpoint) in Apple Documentation
 * @property x Normalized horizontal coordinate; finite values outside 0..1 are permitted.
 * @property y Normalized vertical coordinate; finite values outside 0..1 are permitted.
 */
data class UnitPoint(
    val x: Double,
    val y: Double,
) {
    init {
        require(x.isFinite() && y.isFinite())
    }

    /** Standard native anchors. */
    companion object {
        /** Center of the view. */
        val center = UnitPoint(0.5, 0.5)

        /** Center of the top edge. */
        val top = UnitPoint(0.5, 0.0)

        /** Center of the bottom edge. */
        val bottom = UnitPoint(0.5, 1.0)

        /** Center of the leading edge. */
        val leading = UnitPoint(0.0, 0.5)

        /** Center of the trailing edge. */
        val trailing = UnitPoint(1.0, 0.5)

        /** Top leading corner. */
        val topLeading = UnitPoint(0.0, 0.0)

        /** Top trailing corner. */
        val topTrailing = UnitPoint(1.0, 0.0)

        /** Bottom leading corner. */
        val bottomLeading = UnitPoint(0.0, 1.0)

        /** Bottom trailing corner. */
        val bottomTrailing = UnitPoint(1.0, 1.0)
    }
}
