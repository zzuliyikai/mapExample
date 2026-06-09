package com.hanshow.mapExample.data.model.map

/**
 * navigationPath_with_location data classes
 */
data class NavigationPathData(
    val data: NavigationData,
    val event: String,
    val messageId: String,
    val type: String,
    val time: String,
    val delayTime: Double,
    val currentLocation: CurrentLocation
)

data class NavigationData(
    val positionList: List<NavigationPosition>
)

data class NavigationPosition(
    val distance: Double,
    val isGoods: Boolean,
    val isTarget: Boolean,
    val x: String,
    val y: String,
    val z: String
)

data class CurrentLocation(
    val position: NavigationPositionSimple,
    val heading: NavigationHeading,
    val mainOrientation: NavigationHeading,
    val time: String,
    val timeDiff_seconds: Double
)

data class NavigationPositionSimple(
    val x: String,
    val y: String,
    val z: String
)

data class NavigationHeading(
    val angle: String,
    val x: String,
    val y: String
)