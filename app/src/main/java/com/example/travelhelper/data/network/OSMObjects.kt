package com.example.travelhelper.data.network

import com.google.gson.annotations.SerializedName

data class OverpassResponse(
    @SerializedName("elements") val elements: List<OSMElement>
)

data class OSMElement (
    @SerializedName("type") val type: String,
    @SerializedName("id") val id: Long,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lon") val lon: Double?,
    @SerializedName("center") val center: Center?,
    @SerializedName("tags") val tags: Map<String, String>?
)

data class Center(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double
)
