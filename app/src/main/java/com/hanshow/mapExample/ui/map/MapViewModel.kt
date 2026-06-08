package com.hanshow.mapExample.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanshow.mapExample.data.repository.MapRepository
import com.hanshow.mapExample.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapRepository: MapRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Idle)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun handleIntent(intent: MapIntent) {
        when (intent) {
            is MapIntent.LoadMapData -> loadMapData()
        }
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
}