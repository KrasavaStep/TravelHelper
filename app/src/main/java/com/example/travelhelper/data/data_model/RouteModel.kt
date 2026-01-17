package com.example.travelhelper.data.data_model

import android.os.Parcelable
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable


@Parcelize
data class RouteModel (
    val id: Int = 0,
    val distanceMeters: Int,
    val duration: String,
    val encodedPolyline: String,
    val type: String,
    val description: String,
    val routeName: String,
    val routeCategory: String
): Parcelable