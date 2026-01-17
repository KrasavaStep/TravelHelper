package com.example.travelhelper.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.example.travelhelper.data.data_model.AttractionModel

@Dao
interface AtractionDao {
    @Insert(onConflict = REPLACE)
    suspend fun addAttractionToDB(item: AttractionEntity)

    @Query("SELECT * FROM ATRACTIONS_TABLE")
    suspend fun getAllAttractions(): List<AttractionEntity>

    @Query("SELECT * FROM ATRACTIONS_TABLE WHERE isLiked=1")
    suspend fun getLikedAttractions(): List<AttractionEntity>?

    @Query("SELECT * FROM ROUTES_TABLE")
    suspend fun getRoutes(): List<RoutesEntity>

    @Query("SELECT * FROM ATRACTIONS_TABLE WHERE routeId=:sentRouteId")
    suspend fun getCustomPoints(sentRouteId: Int): List<AttractionEntity>

    @Query("SELECT * FROM ATRACTIONS_TABLE WHERE name LIKE :searchQuery")
    suspend fun searchAttractions(searchQuery: String): List<AttractionEntity>
}