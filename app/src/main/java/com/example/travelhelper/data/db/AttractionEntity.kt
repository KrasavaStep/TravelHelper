package com.example.travelhelper.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "atractions_table",
    indices = [Index("id")],
    /*foreignKeys = [
        ForeignKey(
            entity = CityEntity::class,
            parentColumns = ["id"],
            childColumns = ["cityId"]
        )
    ]*/
)
data class AttractionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cityId: Int = 0,
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val type: String,
    val description: String?,
    val wikipedia: String?,
    val wikidata: String?,
    val website: String?,
    val openingHours: String?,
    val isFee: Boolean = false,
    val isLiked: Boolean = false
)
