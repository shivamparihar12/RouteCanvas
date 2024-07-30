package com.example.routecanvas.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavArgs
import com.example.routecanvas.ui.theme.RouteCanvasTheme

@Composable
fun TrackScreen(id: Int) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = id.toString(), fontSize = 40.sp)
    }
}


@Preview(showBackground = true)
@Composable
fun TrackScreenPreview() {
    RouteCanvasTheme {
        TrackScreen(2)
    }
}