package com.hanshow.mapExample.data.repository

import com.hanshow.mapExample.data.api.AuthApiService
import com.hanshow.mapExample.data.model.auth.LoginRequest
import com.hanshow.mapExample.data.model.auth.LoginResponse
import com.hanshow.mapExample.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService
) {
    fun login(username: String, password: String): Flow<Result<LoginResponse>> = flow {
        emit(Result.Loading)
        try {
            val response = authApiService.login(LoginRequest(username, password))
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "登录失败"))
        }
    }
}