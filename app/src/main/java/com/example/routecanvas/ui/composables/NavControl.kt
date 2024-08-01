package com.example.routecanvas.ui.composables

import android.app.Application
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.routecanvas.repository.TrackRepository

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

    NavHost(navController = navController,
        startDestination = Screens.HomeScreen,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }) {
        composable<Screens.HomeScreen> {
            HomeScreen(
                navigateToAbout = { navController.navigate(Screens.About) },
                navigateToRunning = { navController.navigate(Screens.RunningScreen) },
                navigateToTrackScreen = { id -> navController.navigate(Screens.TrackScreen(id)) },
                trackRepository = trackRepository
            )
        }

        composable<Screens.TrackScreen> {
            val args = it.toRoute<Screens.TrackScreen>()
            TrackScreen(id = args.id, trackRepository = trackRepository) {
                navController.popBackStack()
            }
        }

        composable<Screens.RunningScreen> {
            RunningScreen(application, trackRepository)
        }

        composable<Screens.About> {
            About()
        }
    }
}