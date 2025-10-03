package com.example.travelhelper.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface OverpassAPI {
    @FormUrlEncoded
    @POST("/api/interpreter")
    suspend fun queryOverpass(@Field("data") query: String): Response<OverpassResponse>
}
