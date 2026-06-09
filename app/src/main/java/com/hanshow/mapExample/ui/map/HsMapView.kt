package com.hanshow.mapExample.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.hanshow.hsmap.HsMap
import com.hanshow.hsmap.bean.FloorMapData
import com.hanshow.hsmap.bean.RouteStyle
import com.hanshow.hsmap.bean.SelectionStyle

/**
 * Compose wrapper for HsMap View
 * Uses AndroidView to embed the View-based hsmap drawing library into Compose
 * Returns a MutableState holding the HsMap reference so Compose can call its API
 */
@Composable
fun HsMapView(
    mapData: FloorMapData,
    modifier: Modifier = Modifier
): MutableState<HsMap?> {
    val hsMapRef = remember { mutableStateOf<HsMap?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            HsMap(ctx, null).apply {
                setMapData(mapData)
                setPositionAnimationDuration(1200)
                setScaleRange(0.5f, 10.0f)
                // Set route style for demo
//                routeStyle = RouteStyle(
//                    color = Color.Red.hashCode(),
//                    strokeWidth = 6f,
//                    cornerRadius = 10f,
//                    arrowColor = Color.White.hashCode(),
//                    arrowSpacing = 19f,
//                    arrowLength = 3f
//                )
//                selectionStyle = SelectionStyle(
//                    borderColor = Color.Red.hashCode(),
//                    fillColor = 0x80FF0000.toInt(),
//                    borderWidth = 2f,
//                    borderCornerRadius = 4f
//                )
                hsMapRef.value = this
            }
        },
        update = { hsMap ->
            hsMap.setMapData(mapData)
        }
    )

    return hsMapRef
}