package com.hanshow.mapExample.ui.map

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.hanshow.mapExample.data.model.map.LocationData
import com.hanshow.mapExample.data.model.map.NavigationPathData
import com.hanshow.mapExample.data.repository.MapRepository
import com.hanshow.mapExample.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Position update data emitted during mock position playback.
 * Compose layer uses this to call hsMap.updateStartIcon().
 */
data class PositionUpdate(
    val x: Float,
    val y: Float,
    val angle: Float,
    val delayMs: Long
)

/**
 * Navigator update data emitted during mock navigator playback.
 * Compose layer uses this to call hsMap.updateRoutePathAndCurrentPosition().
 */
data class NavigatorUpdate(
    val routePath: List<Pair<Float, Float>>,
    val x: Float,
    val y: Float,
    val mainOrientationAngle: Float,
    val delayMs: Long
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapRepository: MapRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Idle)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _positionUpdates = MutableSharedFlow<PositionUpdate>(extraBufferCapacity = 64)
    val positionUpdates: SharedFlow<PositionUpdate> = _positionUpdates.asSharedFlow()

    private val _navigatorUpdates = MutableSharedFlow<NavigatorUpdate>(extraBufferCapacity = 64)
    val navigatorUpdates: SharedFlow<NavigatorUpdate> = _navigatorUpdates.asSharedFlow()

    private var mockPositionJob: Job? = null
    private var mockNavigatorJob: Job? = null

    private val gson = Gson()

    fun handleIntent(intent: MapIntent) {
        when (intent) {
            is MapIntent.LoadMapData -> loadMapData()
            is MapIntent.MockPosition -> mockPosition()
            is MapIntent.MockNavigator -> mockNavigator()
            is MapIntent.StopMockPosition -> stopMockPosition()
            is MapIntent.StopMockNavigator -> stopMockNavigator()
        }
    }

    private fun mockPosition() {
        mockPositionJob?.cancel()
        mockNavigatorJob?.cancel()

        mockPositionJob = viewModelScope.launch {
            val json = readJsonFromAssets("mock_position_data.json")
            val locationDataList = gson.fromJson(json, Array<LocationData>::class.java).toList()

            for (locationData in locationDataList) {
                if (!isActive) break

                val x = locationData.data.position.x.toFloat()
                val y = locationData.data.position.y.toFloat()
                val angle = locationData.data.heading.angle.toFloat()

                _positionUpdates.emit(PositionUpdate(x, y, angle, locationData.delay))

                if (locationData.delay > 0) {
                    kotlinx.coroutines.delay(locationData.delay)
                }
            }
        }
    }

    private fun mockNavigator() {
        mockNavigatorJob?.cancel()
        mockPositionJob?.cancel()

        mockNavigatorJob = viewModelScope.launch {
            val json = readJsonFromAssets("navigation_path_data.json")
            val pathDataList = gson.fromJson(json, Array<NavigationPathData>::class.java).toList()

            for (pathData in pathDataList) {
                if (!isActive) break

                // Convert positionList to route path pairs
                val routePath = pathData.data.positionList.map { point ->
                    Pair(point.x.toFloat(), point.y.toFloat())
                }

                // Get current position from first positionList item
                val currentX = pathData.data.positionList.first().x.toFloat()
                val currentY = pathData.data.positionList.first().y.toFloat()
                val mainOrientationAngle = pathData.currentLocation.mainOrientation.angle.toFloat()

                _navigatorUpdates.emit(
                    NavigatorUpdate(
                        routePath = routePath,
                        x = currentX,
                        y = currentY,
                        mainOrientationAngle = mainOrientationAngle,
                        delayMs = pathData.delayTime.toLong()
                    )
                )

                if (pathData.delayTime > 0) {
                    kotlinx.coroutines.delay(pathData.delayTime.toLong())
                }
            }
        }
    }

    private fun stopMockPosition() {
        mockPositionJob?.cancel()
        mockPositionJob = null
    }

    private fun stopMockNavigator() {
        mockNavigatorJob?.cancel()
        mockNavigatorJob = null
    }

    private fun loadMapData() {
        viewModelScope.launch {
            mapRepository.getMapData(23).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.value = MapUiState.Loading()
                    is Result.Success -> _uiState.value = MapUiState.Success(result.data)
                    is Result.Error -> _uiState.value = MapUiState.Error(result.message)
                }
            }
        }
    }

    private fun readJsonFromAssets(fileName: String): String {
        return application.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    override fun onCleared() {
        super.onCleared()
        mockPositionJob?.cancel()
        mockNavigatorJob?.cancel()
    }
}