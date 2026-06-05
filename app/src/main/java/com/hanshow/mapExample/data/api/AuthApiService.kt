package com.hanshow.mapExample.data.api

import com.hanshow.mapExample.data.model.auth.LoginRequest
import com.hanshow.mapExample.data.model.auth.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}