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
}