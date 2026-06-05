package com.hanshow.mapExample.data.api

import com.hanshow.mapExample.data.model.map.MapDataResponse
import retrofit2.http.GET

interface MapApiService {
    @GET("map/data")
    suspend fun getMapData(): MapDataResponse
}