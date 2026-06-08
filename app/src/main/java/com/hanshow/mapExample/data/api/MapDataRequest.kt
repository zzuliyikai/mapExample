package com.hanshow.mapExample.data.api

import kotlinx.serialization.Serializable

@Serializable
data class MapDataRequest(
    val data: Int
)