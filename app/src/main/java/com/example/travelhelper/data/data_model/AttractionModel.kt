package com.example.travelhelper.data.data_model

data class AttractionModel(
    val id: Int = 0,
    val routeId: Int? = null,
    val cityName: String = "Гомель",
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
    val isFee: Boolean = false,
    val isLiked: Boolean = false,
    val isCustom: Boolean = false
)
