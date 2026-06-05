package com.hanshow.mapExample.ui.login

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data class Loading(val message: String = "登录中...") : LoginUiState()
    data class Success(val token: String, val username: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}