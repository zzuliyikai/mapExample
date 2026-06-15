package com.hanshow.mapExample.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hanshow.hsmap.bean.FloorMapData
import com.hanshow.mapExample.data.api.HttpErrorException
import com.hanshow.mapExample.data.api.MapApiService
import com.hanshow.mapExample.data.model.map.GsonMapResponse
import com.hanshow.mapExample.data.model.map.MapDataRequest
import com.hanshow.mapExample.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
            val time = System.currentTimeMillis()

            // Parse the entire response with Gson in ONE pass — no JSONObject triple-parsing
            val responseType = object : TypeToken<GsonMapResponse<FloorMapData>>() {}.type
            val response = gson.fromJson<GsonMapResponse<FloorMapData>>(jsonString, responseType)

            Log.d("render time", "Gson parse time spent = ${System.currentTimeMillis() - time}ms")

            if (response.code == "SUC") {
                if (response.data != null) {
                    emit(Result.Success(response.data))
                } else {
                    emit(Result.Error("No map data found"))
                }
            } else {
                emit(Result.Error(response.message))
            }
        } catch (e: HttpErrorException) {
            emit(Result.Error(e.userMessage))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to load map data"))
        }
    }.flowOn(Dispatchers.IO)
}
