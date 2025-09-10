package com.example.travelhelper.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city_table")
data class CityEntity(
    @PrimaryKey(autoGenerate = true) val id:Int,
    val city_name:String
)
