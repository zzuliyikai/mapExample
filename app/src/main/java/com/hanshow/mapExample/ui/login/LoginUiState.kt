package com.hanshow.mapExample.ui.login

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data class Loading(val message: String = "Logging in...") : LoginUiState()
    data class Success(val accessToken: String, val refreshToken: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}