package com.hanshow.mapExample.ui.settings

sealed class SettingsUiState {
    data object Idle : SettingsUiState()
    data class Loaded(
        val baseUrl: String,
        val customerCode: String,
        val storeCode: String,
        val floorId: Int
    ) : SettingsUiState()
    data object Saved : SettingsUiState()
}
