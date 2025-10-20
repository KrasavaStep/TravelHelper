package com.example.travelhelper.data.network.overpass_api

import com.example.travelhelper.data.data_model.AttractionModel

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
) {
    fun convertToAttractionModel(attraction: OSMPlace, isLiked: Boolean): AttractionModel {
        return AttractionModel(
            name = attraction.name,
            category = attraction.category,
            latitude = attraction.latitude,
            longitude = attraction.longitude,
            type = attraction.type,
            description = attraction.description,
            wikipedia = attraction.wikipedia,
            wikidata = attraction.wikidata,
            website = attraction.website,
            openingHours = attraction.openingHours,
            isFee = attraction.isFee,
            isLiked = isLiked
        )
}
}
