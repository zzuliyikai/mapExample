package com.hanshow.mapExample.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    val resultCode: String,
    val result: String,
    val message: String,
    val data: T? = null
)