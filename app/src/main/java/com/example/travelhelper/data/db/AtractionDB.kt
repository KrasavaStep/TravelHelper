package com.example.travelhelper.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(version = 2, entities = [CityEntity::class, AttractionEntity::class], exportSchema = false)
abstract class AtractionDB: RoomDatabase() {
    abstract fun getAttrationDao(): AtractionDao
}
