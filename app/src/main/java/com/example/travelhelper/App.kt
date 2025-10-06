package com.example.travelhelper

import android.app.Application
import androidx.room.Room
import com.example.travelhelper.data.db.AtractionDB

class App : Application() {
    // Здесь будет храниться единственный экземпляр нашей базы данных
    lateinit var db: AtractionDB

    override fun onCreate() {
        super.onCreate()

        // Этот код выполнится один раз при самом первом запуске приложения.
        // Он создает базу данных.
        db = Room.databaseBuilder(
            applicationContext,
            AtractionDB::class.java, "travel-helper-db" // Имя файла базы данных
        )
            .allowMainThreadQueries() // Позволяет делать запросы из главного потока (упрощение для разработки)
            .build()
    }
}
