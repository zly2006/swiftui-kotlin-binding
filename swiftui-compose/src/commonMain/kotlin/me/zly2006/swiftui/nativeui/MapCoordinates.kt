package me.zly2006.swiftui.nativeui

/** Geographic latitude and longitude in degrees.
 * [CLLocationCoordinate2D](https://developer.apple.com/documentation/corelocation/cllocationcoordinate2d) in Apple Documentation
 * @property latitude Latitude between -90 and 90.
 * @property longitude Longitude between -180 and 180.
 */
data class CLLocationCoordinate2D(
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(latitude.isFinite() && latitude in -90.0..90.0)
        require(longitude.isFinite() && longitude in -180.0..180.0)
    }
}

/** Visible geographic extent in degrees.
 * [MKCoordinateSpan](https://developer.apple.com/documentation/mapkit/mkcoordinatespan) in Apple Documentation
 * @property latitudeDelta Positive latitude extent, at most 180 degrees.
 * @property longitudeDelta Positive longitude extent, at most 360 degrees.
 */
data class MKCoordinateSpan(
    val latitudeDelta: Double,
    val longitudeDelta: Double,
) {
    init {
        require(latitudeDelta.isFinite() && latitudeDelta > 0.0 && latitudeDelta <= 180.0)
        require(longitudeDelta.isFinite() && longitudeDelta > 0.0 && longitudeDelta <= 360.0)
    }
}

/** Controlled MapKit region.
 * [MKCoordinateRegion](https://developer.apple.com/documentation/mapkit/mkcoordinateregion) in Apple Documentation
 * @property center Geographic center of the visible region.
 * @property span Visible geographic extent.
 */
data class MKCoordinateRegion(
    val center: CLLocationCoordinate2D,
    val span: MKCoordinateSpan,
)
