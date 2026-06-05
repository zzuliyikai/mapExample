package com.hanshow.mapExample.ui.map

sealed class MapIntent {
    data object LoadMapData : MapIntent()
}