package com.example.travelhelper.data.db

import androidx.room.Dao
import androidx.room.Query
import com.example.travelhelper.data.data_model.AttractionModel

@Dao
interface AtractionDao {
    @Query("SELECT * FROM ATRACTIONS_TABLE")
    fun getAllAttractions(): List<AttractionEntity>
}