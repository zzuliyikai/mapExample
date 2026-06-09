package com.hanshow.mapExample.data.model.map

/**
 * path.json route data classes
 */
data class PathData(
    val delayTime: Long,
    val pathList: List<PathPoint>
)

data class PathPoint(
    val x: Double,
    val y: Double,
    val isTarget: Boolean
)