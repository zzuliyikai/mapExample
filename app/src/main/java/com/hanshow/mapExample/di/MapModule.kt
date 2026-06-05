package com.hanshow.mapExample.di

import com.hanshow.mapExample.data.api.MapApiService
import com.hanshow.mapExample.data.repository.MapRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapModule {

    @Provides
    @Singleton
    fun provideMapRepository(
        mapApiService: MapApiService
    ): MapRepository = MapRepository(mapApiService)
}