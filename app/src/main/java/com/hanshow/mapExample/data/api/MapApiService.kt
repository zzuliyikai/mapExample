package com.hanshow.mapExample.data.api

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST

interface MapApiService {
    @POST("proxy/mapsweb/mapfloor/get")
    suspend fun getMapData(@Body request: MapDataRequest): ResponseBody
}