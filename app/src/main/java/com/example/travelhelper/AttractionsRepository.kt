package com.example.travelhelper

import com.example.travelhelper.data.db.AtractionDao
import com.example.travelhelper.data.db.AttractionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AttractionsRepository(private val attractionsDao: AtractionDao) {
    suspend fun getAllAttractionsData(): List<AttractionEntity> {
        return withContext(Dispatchers.IO) {
            return@withContext attractionsDao.getAllAttractions()
        }
    }

}