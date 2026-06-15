package com.hanshow.mapExample.ui.map

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import com.hanshow.hsmap.HsMap
import com.hanshow.hsmap.bean.FloorMapData
import com.hanshow.hsmap.bean.SelectionStyle
import androidx.compose.ui.graphics.Color as ComposeColor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                is MapUiState.Loading, is MapUiState.Idle -> LoadingContent()
                is MapUiState.Success -> MapContentSuccess(
                    mapData = (uiState as MapUiState.Success).mapData,
                    viewModel = viewModel,
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
) {
    var isMapRendering by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        val hsMapState = HsMapView(
            mapData = mapData,
            modifier = Modifier.fillMaxSize(),
            onRenderComplete = { isMapRendering = false }
        )

        // Show loading overlay while map is rendering
        if (isMapRendering) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Rendering map...", style = MaterialTheme.typography.bodyLarge)
            }
        }

        // Bottom control panel (always present, but may be behind loading overlay)
        DemoControlPanel(
            hsMap = hsMapState.value,
            mapData = mapData,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ============================================================
// Demo Control Panel
// ============================================================

@Composable
private fun DemoControlPanel(
    hsMap: HsMap?,
    mapData: FloorMapData,
    modifier: Modifier = Modifier
) {
    var elementStr by remember { mutableStateOf("") }
    var rotationEnabled by remember { mutableStateOf(true) }
    var strokeColor by remember { mutableStateOf("#FF0000") }
    var strokeWidth by remember { mutableStateOf("2") }
    var fillColor by remember { mutableStateOf("#00FF00") }
    var elementCode by remember { mutableStateOf("") }
    var scaleMin by remember { mutableStateOf("1") }
    var scaleMax by remember { mutableStateOf("10") }

    LaunchedEffect(hsMap) {
        hsMap?.let {
            it.elementClickEnabled = true
            it.setMaxSelectionCount(3, com.hanshow.hsmap.bean.SelectionMode.REPLACE_EARLIEST)
            it.onElementSelected = { elements ->
                if (elements.isNotEmpty()) {
                    val elementContent = elements.joinToString(", ") { it.toString() }
                    elementStr = "[$elementContent]"
                } else {
                    elementStr = ""
                }
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
            text = elementStr,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Row 2: Map Operations
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            SmallButton(onClick = {
                hsMap?.resetTransformData()
                hsMap?.clearRoutePath()
            }, text = "Reset")

            SmallButton(onClick = {
                hsMap?.setScaleSize(mapData.width / 3f, mapData.height / 2f, 0.8f)
            }, text = "Zoom Out")

            SmallButton(onClick = {
                hsMap?.setScaleSize(mapData.width / 3f, mapData.height / 2f, 1.25f)
            }, text = "Zoom In")

            SmallButton(onClick = {
                rotationEnabled = !rotationEnabled
                hsMap?.rotateEnabled = rotationEnabled
            }, text = if (rotationEnabled) "Rotation OFF" else "Rotation ON")
        }

        // Scale Range & Zoom Input
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = scaleMin,
                        onValueChange = { scaleMin = it },
                        label = {
                            Text(
                                "Scale Min",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = scaleMax,
                        onValueChange = { scaleMax = it },
                        label = {
                            Text(
                                "Scale Max",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SmallButton(
                        onClick = {
                            val min = scaleMin.toFloatOrNull() ?: 1f
                            val max = scaleMax.toFloatOrNull() ?: 10f
                            hsMap?.setScaleRange(min, max)
                        },
                        text = "Apply Scale",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Element Style Input
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ColorInputField(
                        value = strokeColor,
                        onValueChange = { strokeColor = it },
                        label = "Stroke Color",
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = strokeWidth,
                        onValueChange = { strokeWidth = it },
                        label = {
                            Text(
                                "Stroke Width",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ColorInputField(
                        value = fillColor,
                        onValueChange = { fillColor = it },
                        label = "Fill Color",
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = elementCode,
                        onValueChange = { elementCode = it },
                        label = {
                            Text(
                                "Element Code",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
                SmallButton(
                    onClick = {
                        hsMap?.setElementOverlay(
                            elementCode, SelectionStyle(
                                borderColor = strokeColor.toColorInt(),
                                fillColor = fillColor.toColorInt(),
                                borderWidth = strokeWidth.toFloat(),
                                borderCornerRadius = 1f
                            )
                        )
                    },
                    text = "Apply Style",
                    modifier = Modifier.fillMaxWidth()
                )
            }
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

@Composable
private fun ColorInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        modifier = modifier.height(56.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall,
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = try {
                            ComposeColor(value.toColorInt())
                        } catch (_: Exception) {
                            ComposeColor.Transparent
                        },
                        RoundedCornerShape(3.dp)
                    )
            )
        }
    )
}