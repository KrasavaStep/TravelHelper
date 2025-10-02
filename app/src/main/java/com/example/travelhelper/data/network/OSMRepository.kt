package com.example.travelhelper.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class OSMRepository {

    private val overpassService: OverpassAPI

    init {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        val overpassRetrofit = Retrofit.Builder()
            .baseUrl("https://overpass-api.de")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        /* Maybe in future
        val nominatimRetrofit = Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()*/

        overpassService = overpassRetrofit.create(OverpassAPI::class.java)
    }

    suspend fun getAttractions(cityName: String): List<OSMPlace> {
        val query = """
            [out:json][timeout:25];
            area[name="$cityName"]->.searchArea;
            (
              node["tourism"](area.searchArea);
              way["tourism"](area.searchArea);
              relation["tourism"](area.searchArea);
              node["historic"](area.searchArea);
              way["historic"](area.searchArea);
              relation["historic"](area.searchArea);
            );
            out center;
        """.trimIndent()

        return try {
            val response = overpassService.queryOverpass(query)
            if (response.isSuccessful) {
                response.body()?.elements?.mapNotNull { element ->
                    convertToOSMPlace(element)
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun convertToOSMPlace(element: OSMElement): OSMPlace? {
        val name = element.tags?.get("name") ?: return null
        val lat = element.lat ?: element.center?.lat ?: return null
        val lon = element.lon ?: element.center?.lon ?: return null

        return OSMPlace(
            id = element.id,
            name = name,
            category = getCategory(element.tags),
            latitude = lat,
            longitude = lon,
            type = element.type,
            description = element.tags["description"],
            wikipedia = element.tags["wikipedia"],
            website = element.tags["website"]
        )
    }

    private fun getCategory(tags: Map<String, String>?): String {
        if (tags == null) return "other"

        return when {
            tags["tourism"] == "museum" -> "museum"
            tags["tourism"] == "attraction" -> "attraction"
            tags["tourism"] == "viewpoint" -> "viewpoint"
            tags["historic"] == "castle" -> "castle"
            tags["historic"] == "monument" -> "monument"
            tags["amenity"] == "fountain" -> "fountain"
            tags["leisure"] == "park" -> "park"
            else -> "other"
        }
    }

}

