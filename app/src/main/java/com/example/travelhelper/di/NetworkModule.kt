package com.example.travelhelper.di

import com.example.travelhelper.data.network.overpass_api.OverpassAPI
import com.example.travelhelper.data.network.routes_api.RoutesAPI
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
            .client(get(named("AttractionApi")))
            .build()
    }

    single<OkHttpClient>(named("AttractionApi")) {

        OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS).addInterceptor(get<HttpLoggingInterceptor>().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build()
    }

    single<OverpassAPI> {
        (get<Retrofit>(named("AttractionApi"))).create(OverpassAPI::class.java)
    }

    single<OkHttpClient>(named("RoutesApi")) {

        OkHttpClient.Builder().addInterceptor(get<HttpLoggingInterceptor>().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build()
    }

    single<Retrofit>(named("RoutesApi")) {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(RoutesAPI.ROUTES_API_URI)
            .client(get(named("RoutesApi")))
            .build()
    }

    single<RoutesAPI> {
        (get<Retrofit>(named("RoutesApi"))).create(RoutesAPI::class.java)
    }

}