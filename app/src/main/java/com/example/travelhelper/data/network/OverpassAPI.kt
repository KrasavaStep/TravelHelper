package com.example.travelhelper.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OverpassAPI {
    @POST("/api/interpreter")
    suspend fun queryOverpass(@Body query: String): Response<OverpassResponse>
}
