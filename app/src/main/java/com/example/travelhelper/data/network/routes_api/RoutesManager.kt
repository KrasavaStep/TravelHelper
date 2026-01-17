package com.example.travelhelper.data.network.routes_api

import android.content.Context
import android.util.Log
import com.example.travelhelper.BuildConfig
import com.google.maps.android.PolyUtil

class RoutesManager(
    private val context: Context,
    private val routesApiService: RoutesAPI
) {

    suspend fun calculateRoute(
        origin: LatLng,
        destination: LatLng
    ): Route? {

        val request = RoutesRequest(
            origin = Waypoint(Location(origin)),
            destination = Waypoint(Location(destination))
        )

        return try {
            val response = routesApiService.computeRoutes(apiKey = BuildConfig.ROUTES_API_KEY, request = request)
            if (response.isSuccessful) {
                response.body()?.routes?.first()
            } else {
                null
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun calculateCustomRoute(
        origin: LatLng,
        destination: LatLng,
        intermediates: List<LatLng>
    ): Route? {

        val request = RoutesRequestWithIntermediates(
            origin = Waypoint(Location(origin)),
            destination = Waypoint(Location(destination)),
            intermediates = intermediates.map { Waypoint(Location(it)) }
        )

        return try {
            val response = routesApiService.computeRoutesWithIntermediates(apiKey = BuildConfig.ROUTES_API_KEY, request = request)
            if (response.isSuccessful) {
                response.body()?.routes?.first()
            } else {
                null
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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