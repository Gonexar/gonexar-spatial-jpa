package org.gonexar.data.dto

data class RouteProximityResult(
    val distanceMeters: Double?,
    val intersected: Boolean?,
    val closestPointWkt: String?,
    val intersectionGeometryWkt: String?,
    val intersectionLengthMeters: Double?,
    val routeLengthMeters: Double?,
    val percentRouteInside: Double?,
    val bearingAtClosestPoint: Double? // radians per ST_Azimuth (PostGIS) — convert to degrees if preferir
)