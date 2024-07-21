package com.example.routecanvas.ui.composables

import kotlinx.serialization.Serializable

sealed class Screens {
    @Serializable
    object HomeScreen

    @Serializable
    data class TrackScreen(val id: Int)
}