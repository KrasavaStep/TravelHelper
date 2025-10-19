package com.example.travelhelper.data.network.routes_api

import com.google.gson.annotations.SerializedName
data class RoutesApiResponse(
    @SerializedName("routes") val routes: List<Route>
)

data class Route(
    @SerializedName("distanceMeters") val distanceMeters: Int,
    @SerializedName("duration") val duration: String,
    @SerializedName("polyline") val polyline: Polyline,
    @SerializedName("legs") val legs: List<Leg>
)

data class Polyline(
    @SerializedName("encodedPolyline") val encodedPolyline: String
)

data class Leg(
    @SerializedName("distanceMeters") val distanceMeters: Int,
    @SerializedName("duration") val duration: String,
    @SerializedName("polyline") val polyline: Polyline,
    @SerializedName("startLocation") val startLocation: Location,
    @SerializedName("endLocation") val endLocation: Location
)

data class Location(
    @SerializedName("latLng") val latLng: LatLng
)

data class LatLng(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

data class RoutesRequest(
    val origin: Waypoint,
    val destination: Waypoint,
    val travelMode: String = "WALK",
    //val routingPreference: String = "TRAFFIC_AWARE",
    val polylineQuality: String = "HIGH_QUALITY"
)

data class RoutesRequestWithIntermediates(
    val origin: Waypoint,
    val destination: Waypoint,
    val intermediates: List<Waypoint>,
    val travelMode: String = "WALK",
    //val routingPreference: String = "TRAFFIC_AWARE",
    val polylineQuality: String = "HIGH_QUALITY"
)

data class Waypoint(
    val location: Location
)

