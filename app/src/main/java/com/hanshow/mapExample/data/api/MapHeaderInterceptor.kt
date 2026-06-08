package com.hanshow.mapExample.data.api

import com.hanshow.mapExample.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID
import javax.inject.Inject

/**
 * Map API header interceptor
 * Adds required headers: Authorization, x-ns-customer-code, x-ns-store-code, x-ns-username,
 * x-ns-request-id, x-ns-timestamp, x-ns-signature, X-Hs-customer
 */
class MapHeaderInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val accessToken = kotlinx.coroutines.runBlocking { tokenManager.getAccessToken() }
        val tokenType = kotlinx.coroutines.runBlocking { tokenManager.getTokenType() }
        val timestamp = System.currentTimeMillis().toString()
        val requestId = UUID.randomUUID().toString()

        val newRequest = request.newBuilder()
            .addHeader("Authorization", "${tokenType ?: "Bearer"} $accessToken")
            .addHeader("x-ns-customer-code", ApiConfig.CUSTOMER_CODE)
            .addHeader("x-ns-store-code", ApiConfig.STORE_CODE)
            .addHeader("x-ns-request-id", requestId)
            .addHeader("x-ns-timestamp", timestamp)
            .addHeader("x-ns-signature", computeSignature(timestamp, requestId))
            .addHeader("X-Hs-customer", ApiConfig.CUSTOMER_CODE)
            .build()

        return chain.proceed(newRequest)
    }

    private fun computeSignature(timestamp: String, requestId: String): String {
        // TODO: Implement actual signature algorithm based on Allstar docs
        // The signature is likely computed from: timestamp + requestId + body + secret key
        return ""
    }
}