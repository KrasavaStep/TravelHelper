package com.example.travelhelper.di

import com.example.travelhelper.data.db.AtractionDao
import com.example.travelhelper.data.db.AttractionsRepository
import com.example.travelhelper.data.network.OSMRepository
import com.example.travelhelper.data.network.OverpassAPI
import org.koin.dsl.module

val dataModule = module {

    single<AttractionsRepository> {
        AttractionsRepository(
            get<AtractionDao>(),
        )
    }

    single<OSMRepository> {
        OSMRepository(
            get<OverpassAPI>()
        )
    }
}