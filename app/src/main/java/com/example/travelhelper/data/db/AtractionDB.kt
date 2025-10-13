package com.example.travelhelper.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(version=2, exportSchema = false, entities = [CityEntity::class, AttractionEntity::class, RoutesEntity::class])
abstract class AtractionDB:RoomDatabase() {
    abstract fun getAttrationDao():AtractionDao
}