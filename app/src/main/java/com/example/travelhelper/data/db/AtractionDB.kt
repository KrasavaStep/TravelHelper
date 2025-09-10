package com.example.travelhelper.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(version=1, entities = [CityEntity::class, AttractionEntity::class])
abstract class AtractionDB:RoomDatabase() {
    abstract fun getAttrationDao():AtractionDao
}