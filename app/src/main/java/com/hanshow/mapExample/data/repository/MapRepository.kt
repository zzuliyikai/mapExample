package com.hanshow.mapExample.data.repository

import com.google.gson.Gson
import com.hanshow.hsmap.bean.FloorMapData
import com.hanshow.mapExample.data.api.HttpErrorException
import com.hanshow.mapExample.data.api.MapApiService
import com.hanshow.mapExample.data.api.MapDataRequest
import com.hanshow.mapExample.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import javax.inject.Inject

class MapRepository @Inject constructor(
    private val mapApiService: MapApiService
) {
    private val gson = Gson()

    fun getMapData(floorId: Int): Flow<Result<FloorMapData>> = flow {
        emit(Result.Loading)
        try {
            val responseBody = mapApiService.getMapData(MapDataRequest(floorId))
            val jsonString = responseBody.string()
            val json = JSONObject(jsonString)

            val code = json.optString("code", "")
            val message = json.optString("message", "")

            if (code == "SUC") {
                val dataJson = json.optJSONObject("data")
                if (dataJson != null) {
                    val floorMapData = gson.fromJson(dataJson.toString(), FloorMapData::class.java)
                    emit(Result.Success(floorMapData))
                } else {
                    emit(Result.Error("No map data found"))
                }
            } else {
                emit(Result.Error(message))
            }
        } catch (e: HttpErrorException) {
            emit(Result.Error(e.userMessage))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to load map data"))
        }
    }
}