package com.example.travelhelper.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.travelhelper.data.db.AtractionDB
import com.example.travelhelper.data.db.AtractionDao
import org.koin.dsl.module

val databaseModule = module {

    single { provideDatabase(get()) }
    single { provideDao(get()) }

}

fun provideDatabase(application: Application): AtractionDB {

    return Room.databaseBuilder(
            application,
            AtractionDB::class.java,
            "attraction_db"
        ).createFromAsset("databases/attraction_db_asset.db")
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .fallbackToDestructiveMigration()
            .build()
    }


fun provideDao(db: AtractionDB): AtractionDao {
    return db.getAttrationDao()
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Оставьте пустым, если изменения только в Entity классах
        // или добавьте SQL команды для изменения схемы
    }
}