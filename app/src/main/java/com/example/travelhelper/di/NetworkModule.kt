package com.example.travelhelper.di

import com.example.travelhelper.BuildConfig
import com.example.travelhelper.data.network.OverpassAPI
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single<HttpLoggingInterceptor> {
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    single<Retrofit>(named("AttractionApi")) {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(OverpassAPI.OVERPASS_API_URL)
            .client(get())
            .build()
    }

    single<OkHttpClient> {

        OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS).addInterceptor(get<HttpLoggingInterceptor>().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build()
    }

    single<OverpassAPI> {
        (get<Retrofit>(named("AttractionApi"))).create(OverpassAPI::class.java)
    }


}