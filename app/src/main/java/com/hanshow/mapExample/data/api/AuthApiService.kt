package com.hanshow.mapExample.data.api

import com.hanshow.mapExample.data.model.auth.LoginRequest
import com.hanshow.mapExample.data.model.auth.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("proxy/allstar/v2/oauth/pda/token")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}