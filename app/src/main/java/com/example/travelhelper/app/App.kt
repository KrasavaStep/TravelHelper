package com.example.travelhelper.app

import android.app.Application
import com.example.travelhelper.di.appModule
import com.example.travelhelper.di.dataModule
import com.example.travelhelper.di.databaseModule
import com.example.travelhelper.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(listOf(networkModule, dataModule, appModule, databaseModule))
        }
    }
}