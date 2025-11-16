package com.example.travelhelper.di

import com.example.travelhelper.data.db.AtractionDao
import com.example.travelhelper.data.db.AttractionsRepository
import com.example.travelhelper.data.network.overpass_api.OSMRepository
import com.example.travelhelper.data.network.overpass_api.OverpassAPI
import com.example.travelhelper.data.network.routes_api.RoutesManager
import com.example.travelhelper.utils.GsonParser
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

    single<RoutesManager> {
        RoutesManager(get(), get())
    }

    single<GsonParser> {
        GsonParser(get())
    }
}