package com.example.travelhelper.data.network.routes_api

data class RoutesApiResponse(
    val routes: List<Route>
)

data class Route(
    val distanceMeters: Int,
    val duration: String,
    val polyline: Polyline,
    val legs: List<Leg>
)

data class Polyline(
    val encodedPolyline: String
)

data class Leg(
    val distanceMeters: Int,
    val duration: String,
    val polyline: Polyline,
    val startLocation: Location,
    val endLocation: Location
)

data class Location(
    val latLng: LatLng
)

data class LatLng(
    val latitude: Double,
    val longitude: Double
)

data class RoutesRequest(
    val origin: Waypoint,
    val destination: Waypoint,
    val travelMode: String = "WALK",
    //val routingPreference: String = "TRAFFIC_AWARE",
    val polylineQuality: String = "HIGH_QUALITY"
)

data class Waypoint(
    val location: Location
)