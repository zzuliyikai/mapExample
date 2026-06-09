package com.hanshow.mapExample.ui.map

sealed class MapIntent {
    data object LoadMapData : MapIntent()
    data object MockPosition : MapIntent()
    data object MockNavigator : MapIntent()
    data object StopMockPosition : MapIntent()
    data object StopMockNavigator : MapIntent()
}