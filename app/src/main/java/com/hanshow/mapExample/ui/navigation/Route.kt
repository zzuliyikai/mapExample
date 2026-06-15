package com.hanshow.mapExample.ui.navigation

sealed class Route(val route: String) {
    data object Login : Route("login")
    data object Map : Route("map")
    data object Settings : Route("settings")
}