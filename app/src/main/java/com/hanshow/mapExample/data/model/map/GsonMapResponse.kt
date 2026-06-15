package com.hanshow.mapExample.data.model.map

/**
 * Gson-only response class matching the actual API format: { "code", "message", "data" }
 * Used to parse the entire response in one pass, avoiding JSONObject triple-parsing overhead.
 * Not annotated with @Serializable because Gson handles it via reflection.
 */
data class GsonMapResponse<T>(
    val code: String = "",
    val message: String = "",
    val data: T? = null
)
