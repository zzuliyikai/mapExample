package com.hanshow.mapExample.ui.settings

import androidx.lifecycle.ViewModel
import com.hanshow.mapExample.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.LoadSettings -> loadSettings()
            is SettingsIntent.SaveSettings -> saveSettings(
                intent.baseUrl, intent.customerCode, intent.storeCode, intent.floorId
            )
        }
    }

    private fun loadSettings() {
        _uiState.value = SettingsUiState.Loaded(
            baseUrl = settingsRepository.getBaseUrl(),
            customerCode = settingsRepository.getCustomerCode(),
            storeCode = settingsRepository.getStoreCode(),
            floorId = settingsRepository.getFloorId()
        )
    }

    private fun saveSettings(
        baseUrl: String,
        customerCode: String,
        storeCode: String,
        floorId: Int
    ) {
        settingsRepository.saveBaseUrl(baseUrl)
        settingsRepository.saveCustomerCode(customerCode)
        settingsRepository.saveStoreCode(storeCode)
        settingsRepository.saveFloorId(floorId)
        _uiState.value = SettingsUiState.Saved
    }
}
