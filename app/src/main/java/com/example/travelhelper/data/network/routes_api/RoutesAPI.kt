package com.example.travelhelper.data.network.routes_api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

// RoutesAPI.kt
interface RoutesAPI {

    @POST("directions/v2:computeRoutes")
    suspend fun computeRoutes(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Header("X-Goog-FieldMask") fields: String = "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline,routes.legs",
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: RoutesRequest
    ): Response<RoutesApiResponse>

    companion object {
        const val ROUTES_API_URI = "https://routes.googleapis.com/"
    }
}