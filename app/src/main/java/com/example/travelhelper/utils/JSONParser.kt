package com.example.travelhelper.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.yandex.mapkit.geometry.Point

data class GeoJson(
    @SerializedName("type") val type: String,
    @SerializedName("features") val features: List<Feature>
)

data class Feature(
    @SerializedName("geometry") val geometry: Geometry,
    @SerializedName("type") val type: String,
    @SerializedName("properties") val properties: Properties,
    @SerializedName("id") val id: Int
)

data class Properties(
    @SerializedName("source") val source: String,
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)

data class Geometry(
    @SerializedName("type") val type: String,
    @SerializedName("coordinates") val coordinates: List<List<List<Double>>>
)

data class BorderData(
    val name: String,
    val points: List<Point>
)

class GsonParser(private val context: Context) {

    private val gson = Gson()

    fun parseBorderWithGson(): List<BorderData>? {
        return try {
            val inputStream = context.assets.open("by.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val geoJson = gson.fromJson(jsonString, GeoJson::class.java)
            Log.w("GEOJSON", geoJson.toString())
            convertToBorderData(geoJson)
        } catch (e: Exception) {
            Log.e("GEOJSON", e.toString())
            e.printStackTrace()
            null
        }
    }

    private fun convertToBorderData(geoJson: GeoJson): List<BorderData>? {
        return if (geoJson.features.isNotEmpty()) {

            val listOfBorders = mutableListOf<BorderData>()

            geoJson.features.forEach { feature ->
                val coordinates = feature.geometry.coordinates[0] // Первый полигон
                val points = coordinates.map { coord ->
                    Point(coord[1], coord[0]) // GeoJSON: [lng, lat], Yandex: Point(lat, lng)
                }
                listOfBorders.add(BorderData(feature.properties.name, points))
            }

            listOfBorders
        } else {
            null
        }
    }
}