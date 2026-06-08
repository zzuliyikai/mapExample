package com.hanshow.mapExample.ui.map

import android.graphics.Bitmap
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hanshow.hsmap.HsMap
import com.hanshow.hsmap.bean.FloorMapData
import com.hanshow.hsmap.bean.LocationMode
import com.hanshow.hsmap.bean.ShapeInfo
import com.hanshow.hsmap.bean.ShapeInfoDesc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onLogout: () -> Unit = {},
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var locationMode by remember { mutableStateOf(LocationMode.FREE) }

    // Auto-load map data on entering the page
    LaunchedEffect(Unit) {
        viewModel.handleIntent(MapIntent.LoadMapData)
    }

    // Error message display
    LaunchedEffect(uiState) {
        if (uiState is MapUiState.Error) {
            snackbarHostState.showSnackbar((uiState as MapUiState.Error).message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Map Demo") },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is MapUiState.Idle -> {
                    // Waiting to load
                }

                is MapUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                is MapUiState.Success -> {
                    val mapData = (uiState as MapUiState.Success).mapData
                    Box(modifier = Modifier.fillMaxSize()) {
                        val hsMapState = HsMapView(
                            mapData = mapData,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Floating control panel at bottom
                        DemoControlPanel(
                            hsMap = hsMapState.value,
                            mapData = mapData,
                            locationMode = locationMode,
                            snackbarHostState = snackbarHostState,
                            onLocationModeChanged = { locationMode = it },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }

                is MapUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = (uiState as MapUiState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            viewModel.handleIntent(MapIntent.LoadMapData)
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DemoControlPanel(
    hsMap: HsMap?,
    mapData: FloorMapData,
    locationMode: LocationMode,
    snackbarHostState: SnackbarHostState,
    onLocationModeChanged: (LocationMode) -> Unit,
    modifier: Modifier = Modifier
) {
    // Set up callbacks when hsMap becomes available
    LaunchedEffect(hsMap) {
        hsMap?.let {
            it.elementClickEnabled = true
            it.setMaxSelectionCount(3, com.hanshow.hsmap.bean.SelectionMode.REPLACE_EARLIEST)
            it.onElementSelected = { elements ->
                val names = elements.map { e -> e.name ?: e.code }.joinToString(", ")
                kotlinx.coroutines.runBlocking {
                    snackbarHostState.showSnackbar("Selected: $names")
                }
            }
            it.onLocationModeChanged = { mode ->
                onLocationModeChanged(mode)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Location mode indicator
        Text(
            text = "Mode: ${locationMode.name}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Action buttons row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Route demo
            Button(onClick = {
                val cx = mapData.width / 2f
                val cy = mapData.height / 2f
                hsMap?.addRoutePath(
                    listOf(
                        cx * 0.2f to cy * 0.3f,
                        cx * 0.5f to cy * 0.3f,
                        cx * 0.5f to cy * 0.7f,
                        cx * 0.8f to cy * 0.7f
                    )
                )
            }) {
                Text("Route")
            }

            // Clear route
            Button(onClick = {
                hsMap?.clearRoutePath()
            }) {
                Text("Clear")
            }

            // Position icon demo
            Button(onClick = {
                val cx = (mapData.width / 3f).toInt()
                val cy = (mapData.height / 2f).toInt()
                val iconBitmap = createDemoIconBitmap()
                val arrowBitmap = createDemoArrowBitmap()
                val shapeInfo = ShapeInfo(
                    positionX = cx,
                    positionY = cy,
                    width = 80,
                    height = 80,
                    bitmap = iconBitmap,
                    onClickListener = null,
                    headingAngle = 45f,
                    shapeDesc = ShapeInfoDesc(
                        width = 30,
                        height = 30,
                        bitmap = arrowBitmap,
                        marginBottom = 50
                    )
                )
                hsMap?.updateStartIcon(shapeInfo)
                hsMap?.showCenterPosition(shapeInfo.positionX?.toFloat() ?: 0f, shapeInfo.positionY?.toFloat() ?: 0f, hsMap.getCurrentScaleSize())
            }) {
                Text("Position")
            }

            // Follow mode
            Button(onClick = {
                hsMap?.setLocationMode(LocationMode.FOLLOW)
                onLocationModeChanged(LocationMode.FOLLOW)
            }) {
                Text("Follow")
            }

            // Free mode
            Button(onClick = {
                hsMap?.setLocationMode(LocationMode.FREE)
                onLocationModeChanged(LocationMode.FREE)
            }) {
                Text("Free")
            }

            // Scale 3m
            Button(onClick = {
                hsMap?.setScaleSize((mapData.width / 3f), (mapData.height / 2f), 0.8f)
            }) {
                Text("zoom out")
            }

            // Scale 10m
            Button(onClick = {
                hsMap?.setScaleSize((mapData.width / 3f), (mapData.height / 2f),1.25f)
            }) {
                Text("zoom in")
            }

            // Reset
            Button(onClick = {
                hsMap?.resetTransformData()
                hsMap?.clearRoutePath()
            }) {
                Text("Reset")
            }

            // Rotation demo
            Button(onClick = {
                hsMap?.updateRotationAngle(90f)
            }) {
                Text("plan route")
            }
        }
    }
}

/**
 * Create a simple colored circle bitmap for demo position icon
 */
private fun createDemoIconBitmap(): Bitmap {
    val size = 80
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint()
    paint.color = android.graphics.Color.BLUE
    paint.isAntiAlias = true
    canvas.drawCircle(size / 2f, size / 2f, size / 2f * 0.8f, paint)
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 2f * 0.3f, paint)
    return bitmap
}

/**
 * Create a simple arrow bitmap for demo direction indicator
 */
private fun createDemoArrowBitmap(): Bitmap {
    val size = 30
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint()
    paint.color = android.graphics.Color.RED
    paint.isAntiAlias = true
    // Draw a simple upward arrow triangle
    val path = android.graphics.Path()
    path.moveTo(size / 2f, 0f)
    path.lineTo(0f, size.toFloat())
    path.lineTo(size.toFloat(), size.toFloat())
    path.close()
    canvas.drawPath(path, paint)
    return bitmap
}