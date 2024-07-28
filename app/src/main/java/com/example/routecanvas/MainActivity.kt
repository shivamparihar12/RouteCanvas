package com.example.routecanvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routecanvas.db.TrackDatabase
import com.example.routecanvas.repository.TrackRepository
import com.example.routecanvas.ui.composables.NavControl
import com.example.routecanvas.ui.theme.RouteCanvasTheme
import com.example.routecanvas.viewmodel.LocationViewModel
import com.example.routecanvas.viewmodel.LocationViewModelFactory
import com.example.routecanvas.viewmodel.TrackViewModel

class MainActivity : ComponentActivity() {
    private lateinit var trackRepository: TrackRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trackRepository = TrackRepository(TrackDatabase(this))
        enableEdgeToEdge()
        setContent {
            RouteCanvasTheme {
                NavControl(application, trackRepository)
            }
        }

    }
}