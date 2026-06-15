package com.hanshow.mapExample.ui.map

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.hanshow.hsmap.HsMap
import com.hanshow.hsmap.bean.FloorMapData

/**
 * Compose wrapper for HsMap View
 * Uses AndroidView to embed the View-based hsmap drawing library into Compose
 * Returns a MutableState holding the HsMap reference so Compose can call its API
 *
 * @param onRenderComplete callback invoked when map rendering finishes
 */
@Composable
fun HsMapView(
    mapData: FloorMapData,
    modifier: Modifier = Modifier,
    onRenderComplete: () -> Unit = {}
): MutableState<HsMap?> {
    val hsMapRef = remember { mutableStateOf<HsMap?>(null) }
    // Track the mapData that has been set, to avoid re-setting on every recomposition
    var lastMapData by remember { mutableStateOf<FloorMapData?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            // Factory: only create the View with minimal setup — NO heavy setMapData here
            // This allows the UI to render the layout first (showing a loading overlay)
            HsMap(ctx, null).apply {
                setScaleRange(1f, 10.0f)
                hsMapRef.value = this
            }
        },
        update = { hsMap ->
            // Only set map data when it actually changes (not on every recomposition)
            if (lastMapData != mapData) {
                lastMapData = mapData
                val time = System.currentTimeMillis()
                // Use post {} to defer setMapData to the next main-thread message
                // This lets the current layout pass complete first (loading overlay appears)
                // then setMapData runs, and the render-complete callback hides the overlay
                hsMap.post {
                    hsMap.setMapData(mapData)
                    hsMap.setOnRenderCompleteListener {
                        Log.d("render time", "Render complete time spent = ${System.currentTimeMillis() - time}ms")
                        onRenderComplete()
                    }
                }
            }
        }
    )

    return hsMapRef
}