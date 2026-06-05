package com.hanshow.mapExample.data.model.map

import kotlinx.serialization.Serializable

@Serializable
data class MapNode(
    val id: String,
    val x: Float,
    val y: Float,
    val label: String,
    val type: String = "default" // default, highlight, warning 等
)

@Serializable
data class MapEdge(
    val fromId: String,
    val toId: String,
    val label: String = "",
    val style: String = "solid" // solid, dashed 等
)

@Serializable
data class MapDataResponse(
    val nodes: List<MapNode>,
    val edges: List<MapEdge>,
    val width: Float,
    val height: Float
)