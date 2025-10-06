package com.example.travelhelper.data.db

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AtractionDao {

    @Query("SELECT * FROM ATRACTIONS_TABLE")
    fun getAllAttractions(): List<AttractionEntity>

    @Query("UPDATE atractions_table SET isLiked = :isLiked WHERE id = :attractionId")
    suspend fun setLikedStatus(attractionId: Int, isLiked: Boolean)


    @Query("SELECT * FROM atractions_table WHERE isLiked = 1")
    fun getLikedAttractions(): Flow<List<AttractionEntity>>

    @Query("SELECT isLiked FROM atractions_table WHERE id = :attractionId")
    fun isPlaceLiked(attractionId: Int): Flow<Boolean>
}
