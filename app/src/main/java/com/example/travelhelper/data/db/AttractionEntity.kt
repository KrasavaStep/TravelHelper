package com.example.travelhelper.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "atractions_table", indices = [Index("id")], foreignKeys = [
        ForeignKey(
            entity = CityEntity::class,
            parentColumns = ["id"],
            childColumns = ["city_id"]
        )
    ]
)
data class AttractionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val city_id: Int,
    val addres: String,
    val discription: String,
    val foto: String,
    val dolgota: Float,
    val shirota: Float
)
