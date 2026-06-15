package com.hanshow.mapExample

import android.app.Application
import com.hanshow.mapExample.data.repository.SettingsRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MapExampleApp : Application() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        // Load saved settings into ApiConfig before any network calls
        settingsRepository.loadSettings()
    }
}
