package com.hanshow.mapExample.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanshow.mapExample.data.repository.AuthRepository
import com.hanshow.mapExample.util.Result
import com.hanshow.mapExample.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.Login -> login(intent.username, intent.password)
        }
    }

    private fun login(username: String, password: String) {
        viewModelScope.launch {
            authRepository.login(username, password).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.value = LoginUiState.Loading()
                    is Result.Success -> {
                        val loginData = result.data.data!!
                        tokenManager.saveToken(
                            accessToken = loginData.accessToken,
                            refreshToken = loginData.refreshToken,
                            tokenType = loginData.tokenType,
                            expiresIn = loginData.expiresIn,
                            remoteLogin = loginData.remoteLogin
                        )
                        _uiState.value = LoginUiState.Success(
                            accessToken = loginData.accessToken,
                            refreshToken = loginData.refreshToken
                        )
                    }
                    is Result.Error -> _uiState.value = LoginUiState.Error(result.message)
                }
            }
        }
    }
}