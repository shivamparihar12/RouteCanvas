package com.example.routecanvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.routecanvas.ui.composables.NavControl
import com.example.routecanvas.ui.theme.RouteCanvasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RouteCanvasTheme {
                NavControl(application)
            }
        }

    }
}