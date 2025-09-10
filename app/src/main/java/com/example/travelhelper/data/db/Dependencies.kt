package com.example.travelhelper.data.db

import android.content.Context
import androidx.room.Room

object Dependencies {
    private lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context
    }

    private val appDatabase: AtractionDB by lazy {
        Room.databaseBuilder(applicationContext, AtractionDB::class.java, "database.db")
            .createFromAsset("room_article.db")
            .build()
    }

}