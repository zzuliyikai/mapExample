package com.hanshow.mapExample.ui.map

import com.hanshow.hsmap.bean.FloorMapData

sealed class MapUiState {
    data object Idle : MapUiState()
    data class Loading(val message: String = "Loading map data...") : MapUiState()
    data class Success(val mapData: FloorMapData) : MapUiState()
    data class Error(val message: String) : MapUiState()
}