package com.example.routecanvas.ui.composables

import android.app.Application
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

    NavHost(navController = navController, startDestination = Screens.HomeScreen) {
        composable<Screens.HomeScreen> {
            HomeScreen(
                navigateToAbout = { navController.navigate(Screens.About) },
                navigateToRunning = { navController.navigate(Screens.RunningScreen) },
                navigateToTrackScreen = { id -> navController.navigate(Screens.TrackScreen(id)) },
                trackRepository = trackRepository
            )
        }

        composable<Screens.TrackScreen> {
//            val args = it.toRoute<Screens.TrackScreen>()
//            TrackScreen(id = args.id)
                backStackEntry ->
            val args = backStackEntry.toRoute<Screens.TrackScreen>()
            TrackScreen(id = args.id)
        }

        composable<Screens.RunningScreen> {
            RunningScreen(application, trackRepository)
        }

        composable<Screens.About> {
            About()
        }
    }
}