package com.example.routecanvas.ui.composables

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.routecanvas.db.TrackDatabase
import com.example.routecanvas.repository.TrackRepository
import com.example.routecanvas.viewmodel.LocationViewModel
import com.example.routecanvas.viewmodel.LocationViewModelFactory
import com.example.routecanvas.viewmodel.TrackViewModel

@Composable
fun NavControl(application: Application, trackRepository: TrackRepository) {
    val navController = rememberNavController()
//    val locationViewModel: LocationViewModel = viewModel(
//        factory = LocationViewModelFactory(
//            application = application, trackRepository = trackRepository, owner =  LocalSavedStateRegistryOwner.current
//        )
//    )
    // better to declare this  outside the compose, to have only one instance or use DI lib

   /* DisposableEffect(key1 = navController) {
        val listener = navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.route != Screens.RunningScreen.serializer().toString()) {
                locationViewModel.unBindLocationService()
            }
        }
        navController.addOnDestinationChangedListener(listener = listener)
        onDispose { navController.removeOnDestinationChangedListener(listener = listener) }
    }*/

    NavHost(navController = navController, startDestination = Screens.HomeScreen) {
        composable<Screens.HomeScreen> {
            HomeScreen(
                navigateToAbout = { navController.navigate(Screens.About) },
                navigateToRunning = { navController.navigate(Screens.RunningScreen) },
//                trackViewModel=
            )
        }

        composable<Screens.TrackScreen> {
            val args = it.toRoute<Screens.TrackScreen>()
            TrackScreen(id = args.id)
        }

        composable<Screens.RunningScreen> {
            RunningScreen(application,trackRepository)
        }

        composable<Screens.About> {
            About()
        }
    }
}