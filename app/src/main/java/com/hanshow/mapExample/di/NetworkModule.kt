package com.hanshow.mapExample.di

import com.hanshow.mapExample.data.api.AuthApiService
import com.hanshow.mapExample.data.api.ApiConfig
import com.hanshow.mapExample.data.api.HttpErrorInterceptor
import com.hanshow.mapExample.data.api.MapApiService
import com.hanshow.mapExample.data.api.MapHeaderInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideJson(): Json = json

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    @Named("authOkHttpClient")
    fun provideAuthOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        httpErrorInterceptor: HttpErrorInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(httpErrorInterceptor)
        .build()

    @Provides
    @Singleton
    @Named("mapOkHttpClient")
    fun provideMapOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        httpErrorInterceptor: HttpErrorInterceptor,
        mapHeaderInterceptor: MapHeaderInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(httpErrorInterceptor)
        .addInterceptor(mapHeaderInterceptor)
        .build()

    @Provides
    @Singleton
    @Named("authRetrofit")
    fun provideAuthRetrofit(
        @Named("authOkHttpClient") okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.AUTH_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    @Named("mapRetrofit")
    fun provideMapRetrofit(
        @Named("mapOkHttpClient") okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.MAP_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideAuthApiService(
        @Named("authRetrofit") retrofit: Retrofit
    ): AuthApiService = retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideMapApiService(
        @Named("mapRetrofit") retrofit: Retrofit
    ): MapApiService = retrofit.create(MapApiService::class.java)
}