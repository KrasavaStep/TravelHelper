package com.example.travelhelper.data.db

import com.example.travelhelper.data.data_model.AttractionModel
import com.example.travelhelper.data.data_model.RouteModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AttractionsRepository(private val attractionsDao: AtractionDao) {

    suspend fun searchAttractions(query: String): List<AttractionModel> {
        return withContext(Dispatchers.IO) {
            val results = attractionsDao.searchAttractions("%$query%")
            return@withContext results.map { convertToAttractionModel(it) }
        }
    }

    suspend fun getAllAttractionsData(): List<AttractionEntity> {
        return withContext(Dispatchers.IO) {
            return@withContext attractionsDao.getAllAttractions()
        }
    }

    suspend fun addAttractionToDB(attraction: AttractionModel) {
        attractionsDao.addAttractionToDB(convertToAttractionEntity(attraction))
    }

    suspend fun deleteAttractionFromDB(attraction: AttractionModel) {
        attractionsDao.deleteAttractionFromDB(attraction.name, attraction.latitude, attraction.longitude)
    }

    suspend fun getLikedAttractionsData(): List<AttractionModel> {
        return attractionsDao.getLikedAttractions()?.map { convertToAttractionModel(it) } ?: emptyList()
    }

    suspend fun getRoutesData(): List<RouteModel> {
        return attractionsDao.getRoutes().map { convertToRoutesModel(it) }
    }

    suspend fun getCustomPoints(routeId: Int): List<AttractionModel> {
        return attractionsDao.getCustomPoints(routeId).map { convertToAttractionModel(it) }
    }

    private fun convertToRoutesModel(route: RoutesEntity): RouteModel {
        return RouteModel(
            id = route.id,
            distanceMeters = route.distanceMeters,
            duration = route.duration,
            encodedPolyline = route.encodedPolyline,
            type = route.type,
            description = route.description,
            routeCategory = route.routeCategory ?: "",
            routeName = route.routeName ?: ""
        )
    }

    private fun convertToAttractionModel(attraction: AttractionEntity): AttractionModel {
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
            isLiked = attraction.isLiked
        )
    }

    private fun convertToAttractionEntity(attraction: AttractionModel): AttractionEntity {
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
            isLiked = attraction.isLiked
        )
    }
}
