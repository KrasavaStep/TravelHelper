package com.example.travelhelper.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.travelhelper.data.network.routes_api.Polyline
import com.google.gson.annotations.SerializedName

@Entity(tableName = "routes_table")
data class RoutesEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val distanceMeters: Int,
    val duration: String,
    val encodedPolyline: String,
    val type: String,
    val description: String,
    val routeCategory: String,
    val routeName: String? = null
)