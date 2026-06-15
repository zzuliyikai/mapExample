package com.hanshow.mapExample.data.api

import com.hanshow.mapExample.data.model.auth.ErrorCode
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

/**
 * Global HTTP error interceptor
 * Handles non-2xx responses, maps HTTP status codes and business error codes to user-friendly messages
 */
class HttpErrorInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.isSuccessful) {
            return response
        }

        // Non-2xx response: try to parse business error code
        val httpCode = response.code
        val body = response.peekBody(Long.MAX_VALUE).string()

        val errorMsg = try {
            val json = JSONObject(body)
            val resultCode = json.optString("resultCode", "")
            val message = json.optString("message", "")
            if (resultCode.isNotEmpty()) {
                // Prefer business error code mapping
                ErrorCode.fromCode(resultCode, message)
            } else {
                // Fall back to HTTP status code mapping
                mapHttpError(httpCode)
            }
        } catch (_: Exception) {
            // JSON parse failed, fall back to HTTP status code mapping
            mapHttpError(httpCode)
        }

        throw HttpErrorException(httpCode, errorMsg)
    }

    private fun mapHttpError(httpCode: Int): String = when (httpCode) {
        400 -> "Bad request"
        401 -> "Unauthorized, please login again"
        403 -> "Forbidden, please contact admin"
        404 -> "Resource not found"
        408 -> "Request timeout, please try again later"
        429 -> "Too many requests, please try again later"
        500 -> "Internal server error"
        502 -> "Bad gateway"
        503 -> "Service temporarily unavailable"
        504 -> "Gateway timeout"
        else -> "Request failed ($httpCode)"
    }
}

/**
 * Custom HTTP error exception with user-friendly error message
 */
class HttpErrorException(
    val httpCode: Int,
    val userMessage: String
) : IOException(userMessage)