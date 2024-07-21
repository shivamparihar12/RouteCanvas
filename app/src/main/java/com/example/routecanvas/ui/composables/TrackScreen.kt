package com.example.routecanvas.ui.composables

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavArgs

@Composable
fun TrackScreen(id: Int) {
    Text(text = id.toString())
}