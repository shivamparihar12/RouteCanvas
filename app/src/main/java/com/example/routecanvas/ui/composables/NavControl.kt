package com.example.routecanvas.ui.composables

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.routecanvas.MyLocationService
import com.example.routecanvas.viewmodel.LocationViewModel
import com.example.routecanvas.viewmodel.LocationViewModelFactory

@Composable
fun NavControl(application: Application) {
    val navController = rememberNavController()
    val locationViewModel: LocationViewModel =
        viewModel(factory = LocationViewModelFactory(application = application)) // better to declare this  outside the compose, to have only one instance or use DI lib

    NavHost(navController = navController, startDestination = Screens.HomeScreen) {
        composable<Screens.HomeScreen> {
            HomeScreen(
                navigateToAbout = { navController.navigate(Screens.About) },
                navigateToRunning = { navController.navigate(Screens.RunningScreen) })
        }

        composable<Screens.TrackScreen> {
            val args = it.toRoute<Screens.TrackScreen>()
            TrackScreen(id = args.id)
        }

        composable<Screens.RunningScreen> {
            RunningScreen(locationViewModel = locationViewModel)
        }

        composable<Screens.About> {
            About()
        }
    }
}