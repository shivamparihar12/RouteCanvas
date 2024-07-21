package com.example.routecanvas.ui.composables

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

@Composable
fun NavControl() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screens.HomeScreen) {
        composable<Screens.HomeScreen> {
            HomeScreen() {
                navController.navigate(it)
            }
        }

        composable<Screens.TrackScreen> {
            val args = it.toRoute<Screens.TrackScreen>()
            TrackScreen(id = args.id)
        }
    }
}