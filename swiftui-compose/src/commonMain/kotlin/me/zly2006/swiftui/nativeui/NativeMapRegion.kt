package me.zly2006.swiftui.nativeui

/** Geographic state stays in Kotlin; MapKit forwards camera changes through the callback.
 * @property latitude Center latitude in degrees, between -90 and 90.
 * @property longitude Center longitude in degrees, between -180 and 180.
 * @property latitudeSpan Positive visible latitude span in degrees, at most 180.
 * @property longitudeSpan Positive visible longitude span in degrees, at most 360.
 * [MKCoordinateRegion](https://developer.apple.com/documentation/mapkit/mkcoordinateregion) in Apple Documentation
 */
data class NativeMapRegion(
    val latitude: Double,
    val longitude: Double,
    val latitudeSpan: Double,
    val longitudeSpan: Double,
) {
    init {
        require(latitude.isFinite() && latitude in -90.0..90.0)
        require(longitude.isFinite() && longitude in -180.0..180.0)
        require(latitudeSpan.isFinite() && latitudeSpan > 0.0 && latitudeSpan <= 180.0)
        require(longitudeSpan.isFinite() && longitudeSpan > 0.0 && longitudeSpan <= 360.0)
    }
}
