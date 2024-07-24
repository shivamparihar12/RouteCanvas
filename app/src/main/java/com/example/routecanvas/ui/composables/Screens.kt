package com.example.routecanvas.ui.composables

import com.example.routecanvas.viewmodel.LocationViewModel
import kotlinx.serialization.Serializable

sealed class Screens {
    @Serializable
    object HomeScreen : Screens()

    @Serializable
    data class TrackScreen(val id: Int) : Screens()

    @Serializable
    object RunningScreen : Screens()

    @Serializable
    object About : Screens()
}