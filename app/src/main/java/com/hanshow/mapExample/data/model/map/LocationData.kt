package com.hanshow.mapExample.data.model.map

/**
 * ntuc_position.txt data classes
 */
data class LocationData(
    val data: Data,
    val event: String,
    val type: String,
    val delay: Long
)

data class Data(
    val accuracy: String,
    val heading: Heading,
    val mainOrientation: Heading,
    val position: Position
)

data class Heading(
    val angle: Double,
    val x: String,
    val y: String
)

data class Position(
    val x: Double,
    val y: Double,
    val z: String
)