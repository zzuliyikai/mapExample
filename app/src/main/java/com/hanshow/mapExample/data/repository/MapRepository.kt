package com.hanshow.mapExample.data.repository

import com.hanshow.mapExample.data.api.MapApiService
import com.hanshow.mapExample.data.model.map.MapDataResponse
import com.hanshow.mapExample.data.model.map.MapEdge
import com.hanshow.mapExample.data.model.map.MapNode
import com.hanshow.mapExample.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MapRepository @Inject constructor(
    private val mapApiService: MapApiService
) {
    fun getMapData(): Flow<Result<MapDataResponse>> = flow {
        emit(Result.Loading)
        try {
            val response = mapApiService.getMapData()
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "获取地图数据失败"))
        }
    }
}