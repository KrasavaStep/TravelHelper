package com.example.travelhelper.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.travelhelper.data.db.AtractionDB
import com.example.travelhelper.data.db.AtractionDao
import org.koin.dsl.module

val databaseModule = module {

    single { provideDatabase(get()) }
    single { provideDao(get()) }

}

fun provideDatabase(application: Application): AtractionDB {
    return Room.databaseBuilder(application, AtractionDB::class.java, "currency_db")
        .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
        .build()
}


fun provideDao(db: AtractionDB): AtractionDao {
    return db.getAttrationDao()
}