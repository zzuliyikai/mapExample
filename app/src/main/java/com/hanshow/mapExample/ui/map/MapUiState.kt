package com.hanshow.mapExample.ui.map

import com.hanshow.mapExample.data.model.map.MapDataResponse

sealed class MapUiState {
    data object Idle : MapUiState()
    data class Loading(val message: String = "加载地图数据...") : MapUiState()
    data class Success(val mapData: MapDataResponse) : MapUiState()
    data class Error(val message: String) : MapUiState()
}