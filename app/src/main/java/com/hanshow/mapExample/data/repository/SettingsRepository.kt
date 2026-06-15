package com.hanshow.mapExample.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.hanshow.mapExample.data.api.ApiConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    fun loadSettings() {
        ApiConfig.baseUrl = prefs.getString("baseUrl", ApiConfig.baseUrl) ?: ApiConfig.baseUrl
        ApiConfig.CUSTOMER_CODE = prefs.getString("customerCode", ApiConfig.CUSTOMER_CODE) ?: ApiConfig.CUSTOMER_CODE
        ApiConfig.STORE_CODE = prefs.getString("storeCode", ApiConfig.STORE_CODE) ?: ApiConfig.STORE_CODE
        ApiConfig.FLOOR_ID = prefs.getInt("floorId", ApiConfig.FLOOR_ID)
        ApiConfig.userName = prefs.getString("userName", ApiConfig.userName) ?: ApiConfig.userName
        ApiConfig.pwd = prefs.getString("pwd", ApiConfig.pwd) ?: ApiConfig.pwd
    }

    fun saveUserName(name: String) {
        prefs.edit().putString("userName", name).apply()
        ApiConfig.userName = name
    }

    fun savePwd(pwd: String) {
        prefs.edit().putString("pwd", pwd).apply()
        ApiConfig.pwd = pwd
    }


    fun saveBaseUrl(url: String) {
        prefs.edit().putString("baseUrl", url).apply()
        ApiConfig.baseUrl = url
    }

    fun saveCustomerCode(code: String) {
        prefs.edit().putString("customerCode", code).apply()
        ApiConfig.CUSTOMER_CODE = code
    }

    fun saveStoreCode(code: String) {
        prefs.edit().putString("storeCode", code).apply()
        ApiConfig.STORE_CODE = code
    }

    fun saveFloorId(id: Int) {
        prefs.edit().putInt("floorId", id).apply()
        ApiConfig.FLOOR_ID = id
    }

    fun getBaseUrl(): String = prefs.getString("baseUrl", ApiConfig.baseUrl) ?: ApiConfig.baseUrl

    fun getCustomerCode(): String = prefs.getString("customerCode", ApiConfig.CUSTOMER_CODE) ?: ApiConfig.CUSTOMER_CODE

    fun getStoreCode(): String = prefs.getString("storeCode", ApiConfig.STORE_CODE) ?: ApiConfig.STORE_CODE

    fun getFloorId(): Int = prefs.getInt("floorId", ApiConfig.FLOOR_ID)
}
