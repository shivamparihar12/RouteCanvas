package com.example.routecanvas.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.routecanvas.ui.theme.RouteCanvasTheme

@Composable
fun About() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(text = "About", modifier = Modifier.align(Alignment.Center))
    }
}

@Preview(showBackground = true)
@Composable
fun AboutPreview() {
    RouteCanvasTheme {
        About()
    }
}