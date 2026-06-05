package com.hanshow.mapExample.di

import com.hanshow.mapExample.data.api.AuthApiService
import com.hanshow.mapExample.data.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApiService: AuthApiService
    ): AuthRepository = AuthRepository(authApiService)
}