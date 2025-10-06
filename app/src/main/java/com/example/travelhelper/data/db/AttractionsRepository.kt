package com.example.travelhelper.data.db

import com.example.travelhelper.data.network.OSMPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.String

class AttractionsRepository(private val attractionsDao: AtractionDao) {


    suspend fun getAllAttractionsData(): List<AttractionEntity> {
        return withContext(Dispatchers.IO) {
            return@withContext attractionsDao.getAllAttractions()
        }
    }

    suspend fun addAttractionToDB(attraction: OSMPlace) {
        attractionsDao.addAttractionToDB(convertToAttractionEntity(attraction))
    }

    private fun convertToAttractionEntity(attraction: OSMPlace): AttractionEntity {
        return AttractionEntity(
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
            isLiked = true
        )
    }
}