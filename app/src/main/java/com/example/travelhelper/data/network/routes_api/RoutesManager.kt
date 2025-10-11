package com.example.travelhelper.data.network.routes_api

import android.content.Context
import com.example.travelhelper.BuildConfig
import com.google.maps.android.PolyUtil

class RoutesManager(
    private val context: Context,
    private val routesApiService: RoutesAPI
) {

    suspend fun calculateRoute(
        origin: LatLng,
        destination: LatLng
    ): Result<Route> = try {
        val request = RoutesRequest(
            origin = Waypoint(Location(origin)),
            destination = Waypoint(Location(destination))
        )

        val response = routesApiService.computeRoutes(apiKey = BuildConfig.ROUTES_API_KEY, request = request)

        if (response.routes.isNotEmpty()) {
            Result.success(response.routes.first())
        } else {
            Result.failure(Exception("No routes found"))
        }
    } catch (e: Exception) {
        Result.failure(Exception(e.message.toString()))
    }

    fun decodePolyline(encodedPolyline: String): List<LatLng> {
        return PolyUtil.decode(encodedPolyline).map { latLng ->
            LatLng(latLng.latitude, latLng.longitude)
        }
    }

    fun formatDuration(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60

        return when {
            hours > 0 -> "${hours}ч ${minutes}м"
            else -> "${minutes}м"
        }
    }

    fun formatDistance(meters: Int): String {
        return when {
            meters >= 1000 -> "${"%.1f".format(meters / 1000.0)} км"
            else -> "$meters м"
        }
    }
}