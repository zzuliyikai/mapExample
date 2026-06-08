package com.hanshow.mapExample.data.repository

import com.hanshow.mapExample.data.api.HttpErrorException
import com.hanshow.mapExample.data.api.AuthApiService
import com.hanshow.mapExample.data.model.auth.ErrorCode
import com.hanshow.mapExample.data.model.auth.LoginRequest
import com.hanshow.mapExample.data.model.auth.LoginResponse
import com.hanshow.mapExample.util.EncryptUtils
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
            val encryptedPassword = EncryptUtils.md5(password + username)
            val response = authApiService.login(LoginRequest(username, encryptedPassword))
            if (response.resultCode == "1001" && response.result == "SUCCESS") {
                emit(Result.Success(response))
            } else {
                val errorMsg = ErrorCode.fromCode(response.resultCode, response.message)
                emit(Result.Error(errorMsg))
            }
        } catch (e: HttpErrorException) {
            // HTTP error (non-2xx), interceptor already converted to user-friendly message
            emit(Result.Error(e.userMessage))
        } catch (e: Exception) {
            // Other exceptions (network unavailable, timeout, etc.)
            emit(Result.Error(e.message ?: "Network connection failed"))
        }
    }
}