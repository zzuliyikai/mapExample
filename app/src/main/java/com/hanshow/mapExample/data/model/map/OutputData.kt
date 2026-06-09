package com.hanshow.mapExample.data.model.map

/**
 * output.txt data classes
 */
data class OutputData(
    val location: OutputLocation,
    val heading: OutputHeading,
    val mainOrientation: OutputHeading,
    val accuracy: Double,
    val angle: Double,
    val floorId: String,
    val timeDeterminedMilli: Long,
    val id: String,
    val buildingId: String,
    val floorOrder: Int,
    val azimuth: Double,
    val lockProgress: Double,
    val travelDistance: Double,
    val serverTimeUtcIso8601: String,
    val timeBetweenUpdatesMilli: Double
)

data class OutputLocation(
    val x: Double,
    val y: Double,
    val z: Double
)

data class OutputHeading(
    val x: Double,
    val y: Double,
    val angle: Double
)