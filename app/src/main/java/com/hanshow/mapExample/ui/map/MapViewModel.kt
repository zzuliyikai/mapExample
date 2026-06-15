package com.hanshow.mapExample.ui.map

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.hanshow.mapExample.data.api.ApiConfig.FLOOR_ID
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

@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapRepository: MapRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Idle)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun handleIntent(intent: MapIntent) {
        when (intent) {
            is MapIntent.LoadMapData -> loadMapData()
            else -> {}
        }
    }

    private fun loadMapData() {
        viewModelScope.launch {
            mapRepository.getMapData(FLOOR_ID).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.value = MapUiState.Loading()
                    is Result.Success -> _uiState.value = MapUiState.Success(result.data)
                    is Result.Error -> _uiState.value = MapUiState.Error(result.message)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}