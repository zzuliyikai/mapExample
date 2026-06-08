package com.hanshow.mapExample.data.model.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginData(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_in")
    val expiresIn: String,
    val scope: String,
    val userFirstLogin: String,
    val remoteLogin: String,
    val domains: String,
    val passwordModifyExpires: String,
    val googleType: String
)