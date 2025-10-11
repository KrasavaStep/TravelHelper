package com.example.travelhelper.data.network.overpass_api

data class OSMPlace(
    val id: Long,
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val type: String,
    val description: String?,
    val wikipedia: String?,
    val wikidata: String?,
    val website: String?,
    val openingHours: String?,
    val isFee: Boolean = false
)
