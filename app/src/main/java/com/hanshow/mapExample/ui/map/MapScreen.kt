package com.hanshow.mapExample.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hanshow.hsmap.HsMap
import com.hanshow.hsmap.bean.FloorMapData
import com.hanshow.hsmap.bean.LocationMode
import com.hanshow.hsmap.bean.ShapeInfo
import com.hanshow.hsmap.bean.ShapeInfoDesc

// ============================================================
// Enums
// ============================================================

enum class MockMode { NONE, POSITION, NAVIGATOR }

// ============================================================
// Main Screen
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var locationMode by remember { mutableStateOf(LocationMode.FREE) }

    LaunchedEffect(Unit) {
        viewModel.handleIntent(MapIntent.LoadMapData)
    }

    LaunchedEffect(uiState) {
        if (uiState is MapUiState.Error) {
            snackbarHostState.showSnackbar((uiState as MapUiState.Error).message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Map Demo") },
                colors = TopAppBarDefaults.topAppBarColors(
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
                is MapUiState.Idle -> Unit
                is MapUiState.Loading -> LoadingContent()
                is MapUiState.Success -> MapContentSuccess(
                    mapData = (uiState as MapUiState.Success).mapData,
                    viewModel = viewModel,
                    locationMode = locationMode,
                    onLocationModeChanged = { locationMode = it },
                    snackbarHostState = snackbarHostState
                )

                is MapUiState.Error -> ErrorContent(
                    message = (uiState as MapUiState.Error).message,
                    onRetry = { viewModel.handleIntent(MapIntent.LoadMapData) }
                )
            }
        }
    }
}

// ============================================================
// Content States
// ============================================================

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Loading...", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

// ============================================================
// Map Content (Success state)
// ============================================================

@Composable
private fun MapContentSuccess(
    mapData: FloorMapData,
    viewModel: MapViewModel,
    locationMode: LocationMode,
    onLocationModeChanged: (LocationMode) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var mockMode by remember { mutableStateOf(MockMode.NONE) }

    Box(modifier = Modifier.fillMaxSize()) {
        val hsMapState = HsMapView(
            mapData = mapData,
            modifier = Modifier.fillMaxSize()
        )

        // Observe mock data streams
        MockPositionObserver(hsMapState = hsMapState, viewModel = viewModel)
        MockNavigatorObserver(hsMapState = hsMapState, viewModel = viewModel)

        // Bottom control panel
        DemoControlPanel(
            hsMap = hsMapState.value,
            mapData = mapData,
            locationMode = locationMode,
            mockMode = mockMode,
            onMockPositionStart = {
                mockMode = MockMode.POSITION
                viewModel.handleIntent(MapIntent.MockPosition)
            },
            onMockPositionStop = {
                mockMode = MockMode.NONE
                viewModel.handleIntent(MapIntent.StopMockPosition)
            },
            onMockNavigatorStart = {
                mockMode = MockMode.NAVIGATOR
                viewModel.handleIntent(MapIntent.MockNavigator)
            },
            onMockNavigatorStop = {
                mockMode = MockMode.NONE
                viewModel.handleIntent(MapIntent.StopMockNavigator)
                hsMapState.value?.clearRoutePath()
            },
            onLocationModeChanged = onLocationModeChanged,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ============================================================
// Mock Data Observers
// ============================================================

@Composable
private fun MockPositionObserver(
    hsMapState: androidx.compose.runtime.MutableState<HsMap?>,
    viewModel: MapViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.positionUpdates.collect { update ->
            hsMapState.value?.updateStartIcon(
                ShapeInfo(
                    positionX = update.x.toInt(),
                    positionY = update.y.toInt(),
                    width = 100,
                    height = 100,
                    bitmap = createDemoIconBitmap(),
                    onClickListener = null,
                    headingAngle = update.angle,
                    shapeDesc = ShapeInfoDesc(
                        bitmap = createDemoArrowBitmap(),
                        width = 40,
                        height = 40,
                        marginBottom = 60
                    )
                )
            )
        }
    }
}

@Composable
private fun MockNavigatorObserver(
    hsMapState: androidx.compose.runtime.MutableState<HsMap?>,
    viewModel: MapViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.navigatorUpdates.collect { update ->
            hsMapState.value?.updateRoutePathAndCurrentPosition(
                update.routePath,
                ShapeInfo(
                    positionX = update.x.toInt(),
                    positionY = update.y.toInt(),
                    width = 100,
                    height = 100,
                    bitmap = createDemoIconBitmap(),
                    onClickListener = null,
                    headingAngle = null,
                    shapeDesc = ShapeInfoDesc(
                        bitmap = createDemoArrowBitmap(),
                        width = 40,
                        height = 40,
                        marginBottom = 60
                    )
                )
            )
        }
    }
}

// ============================================================
// Demo Control Panel
// ============================================================

@Composable
private fun DemoControlPanel(
    hsMap: HsMap?,
    mapData: FloorMapData,
    locationMode: LocationMode,
    mockMode: MockMode,
    onMockPositionStart: () -> Unit,
    onMockPositionStop: () -> Unit,
    onMockNavigatorStart: () -> Unit,
    onMockNavigatorStop: () -> Unit,
    onLocationModeChanged: (LocationMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(hsMap) {
        hsMap?.let {
            it.elementClickEnabled = true
            it.setMaxSelectionCount(3, com.hanshow.hsmap.bean.SelectionMode.REPLACE_EARLIEST)
            it.onElementSelected = { elements ->
                val codes = elements.map { e -> e.code }.joinToString(", ")
                Log.d("HsMap", "Selected: $codes")
                Toast.makeText(context, "Selected: $codes", Toast.LENGTH_SHORT).show()
            }
            it.onLocationModeChanged = { mode ->
                onLocationModeChanged(mode)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Mode: ${locationMode.name}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Row 1: Simulation & Route
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MockControlButtons(
                mockMode = mockMode,
                onMockPositionStart = onMockPositionStart,
                onMockPositionStop = onMockPositionStop,
                onMockNavigatorStart = onMockNavigatorStart,
                onMockNavigatorStop = onMockNavigatorStop
            )
            SmallButton(onClick = { hsMap?.addRoutePath(demoRoutePath(mapData)) }, text = "Route")
            SmallButton(onClick = { hsMap?.clearRoutePath() }, text = "Clear")
            SmallButton(onClick = { hsMap?.showDemoPosition(mapData) }, text = "Position")
        }

        // Row 2: Map Operations
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SmallButton(onClick = {
                hsMap?.setLocationMode(LocationMode.FOLLOW)
                onLocationModeChanged(LocationMode.FOLLOW)
            }, text = "Follow")

            SmallButton(onClick = {
                hsMap?.setLocationMode(LocationMode.FREE)
                onLocationModeChanged(LocationMode.FREE)
            }, text = "Free")

            SmallButton(onClick = {
                hsMap?.setScaleSize(mapData.width / 3f, mapData.height / 2f, 0.8f)
            }, text = "Zoom Out")

            SmallButton(onClick = {
                hsMap?.setScaleSize(mapData.width / 3f, mapData.height / 2f, 1.25f)
            }, text = "Zoom In")

            SmallButton(onClick = {
                hsMap?.resetTransformData()
                hsMap?.clearRoutePath()
            }, text = "Reset")
        }
    }
}

// ============================================================
// Panel Sub-components
// ============================================================

@Composable
private fun MockControlButtons(
    mockMode: MockMode,
    onMockPositionStart: () -> Unit,
    onMockPositionStop: () -> Unit,
    onMockNavigatorStart: () -> Unit,
    onMockNavigatorStop: () -> Unit
) {
    when (mockMode) {
        MockMode.POSITION -> SmallButton(onClick = onMockPositionStop, text = "Stop Pos")
        MockMode.NAVIGATOR -> SmallButton(onClick = onMockNavigatorStop, text = "Stop Nav")
        MockMode.NONE -> {
            SmallButton(onClick = onMockPositionStart, text = "Mock Pos")
            SmallButton(onClick = onMockNavigatorStart, text = "Mock Nav")
        }
    }
}

@Composable
private fun SmallButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(32.dp),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall)
    }
}

// ============================================================
// HsMap Demo Extensions (action helpers)
// ============================================================

private fun HsMap.showDemoPosition(mapData: FloorMapData) {
    val cx = (mapData.width / 3f).toInt()
    val cy = (mapData.height / 2f).toInt()
    val shapeInfo = ShapeInfo(
        positionX = cx,
        positionY = cy,
        width = 80,
        height = 80,
        bitmap = createDemoIconBitmap(),
        onClickListener = null,
        headingAngle = 45f,
        shapeDesc = ShapeInfoDesc(
            width = 30,
            height = 30,
            bitmap = createDemoArrowBitmap(),
            marginBottom = 50
        )
    )
    updateStartIcon(shapeInfo)
    showCenterPosition(
        shapeInfo.positionX?.toFloat() ?: 0f,
        shapeInfo.positionY?.toFloat() ?: 0f,
        getCurrentScaleSize()
    )
}

private fun demoRoutePath(mapData: FloorMapData): List<Pair<Float, Float>> {
    return listOf(
        2382f to 5623f,
        2050f to 5623f,
        2050f to 5145f,
        2050f to 1623f,
    )
}

// ============================================================
// Bitmap Helpers
// ============================================================

private fun createDemoIconBitmap(): Bitmap {
    val size = 80
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply { isAntiAlias = true }

    paint.color = Color.BLUE
    canvas.drawCircle(size / 2f, size / 2f, size / 2f * 0.8f, paint)

    paint.color = Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 2f * 0.3f, paint)

    return bitmap
}

private fun createDemoArrowBitmap(): Bitmap {
    val size = 30
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = Color.RED
        isAntiAlias = true
    }
    val path = Path().apply {
        moveTo(size / 2f, 0f)
        lineTo(0f, size.toFloat())
        lineTo(size.toFloat(), size.toFloat())
        close()
    }
    canvas.drawPath(path, paint)
    return bitmap
}