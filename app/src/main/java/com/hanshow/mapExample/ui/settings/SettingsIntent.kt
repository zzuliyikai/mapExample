package com.hanshow.mapExample.ui.settings

sealed class SettingsIntent {
    data class SaveSettings(
        val baseUrl: String,
        val customerCode: String,
        val storeCode: String,
        val floorId: Int
    ) : SettingsIntent()

    data object LoadSettings : SettingsIntent()
}
