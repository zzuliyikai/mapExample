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
                        tokenManager.saveToken(
                            token = result.data.token,
                            userId = result.data.userId,
                            username = result.data.username
                        )
                        _uiState.value = LoginUiState.Success(
                            token = result.data.token,
                            username = result.data.username
                        )
                    }
                    is Result.Error -> _uiState.value = LoginUiState.Error(result.message)
                }
            }
        }
    }
}