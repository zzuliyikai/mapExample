package com.hanshow.mapExample.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val TOKEN_TYPE_KEY = stringPreferencesKey("token_type")
        private val EXPIRES_IN_KEY = stringPreferencesKey("expires_in")
        private val REMOTE_LOGIN_KEY = stringPreferencesKey("remote_login")
    }

    suspend fun saveToken(
        accessToken: String,
        refreshToken: String,
        tokenType: String,
        expiresIn: String,
        remoteLogin: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
            prefs[TOKEN_TYPE_KEY] = tokenType
            prefs[EXPIRES_IN_KEY] = expiresIn
            prefs[REMOTE_LOGIN_KEY] = remoteLogin
        }
    }

    suspend fun getAccessToken(): String? {
        return context.dataStore.data.map { prefs -> prefs[ACCESS_TOKEN_KEY] }.first()
    }

    suspend fun getRefreshToken(): String? {
        return context.dataStore.data.map { prefs -> prefs[REFRESH_TOKEN_KEY] }.first()
    }

    suspend fun getTokenType(): String? {
        return context.dataStore.data.map { prefs -> prefs[TOKEN_TYPE_KEY] }.first()
    }

    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN_KEY)
            prefs.remove(REFRESH_TOKEN_KEY)
            prefs.remove(TOKEN_TYPE_KEY)
            prefs.remove(EXPIRES_IN_KEY)
            prefs.remove(REMOTE_LOGIN_KEY)
        }
    }
}