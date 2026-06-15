package com.hanshow.mapExample.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hanshow.mapExample.ui.login.LoginScreen
import com.hanshow.mapExample.ui.map.MapScreen
import com.hanshow.mapExample.ui.settings.SettingsScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Route.Login.route
    ) {
        composable(Route.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.Map.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Route.Settings.route)
                }
            )
        }

        composable(Route.Map.route) {
            MapScreen()
        }

        composable(Route.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
