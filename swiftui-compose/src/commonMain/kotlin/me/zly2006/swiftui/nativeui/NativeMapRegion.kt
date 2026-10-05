package me.zly2006.swiftui.nativeui

/** Geographic state stays in Kotlin; MapKit forwards camera changes through the callback. */
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
