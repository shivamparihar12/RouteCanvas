package com.example.routecanvas.ui.composables

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.routecanvas.db.TrackDatabase
import com.example.routecanvas.repository.TrackRepository
import com.example.routecanvas.ui.theme.RouteCanvasTheme
import com.example.routecanvas.viewmodel.TrackViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackScreen(id: Int, trackRepository: TrackRepository, navigateBack: () -> Unit) {
    val trackViewModel = remember { TrackViewModel(trackRepository) }
    LaunchedEffect(id) {
        trackViewModel.getSpecificTrack(id)
    }

    val specificTrack by trackViewModel.specificTrack.collectAsState()
    Log.d(
        "TrackScreen",
        specificTrack?.trackImageUri.toString() + specificTrack?.date.toString() + specificTrack?.timeLapsed.toString()
    )
    val formattedDate by remember(specificTrack) {
        derivedStateOf {
            specificTrack?.date?.let { date ->
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)
            } ?: ""
        }
    }
    val formattedTime by remember(specificTrack) {
        derivedStateOf {
            specificTrack?.timeLapsed?.let { timeLapsedMillis ->
                val seconds = (timeLapsedMillis / 1000) % 60
                val minutes = (timeLapsedMillis / (1000 * 60)) % 60
                val hours = timeLapsedMillis / (1000 * 60 * 60)
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } ?: "00:00:00"
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = formattedDate,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.onPrimary),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            specificTrack?.let { track ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    TrackImage(
                        trackImageUri = track.trackImageUri,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TrackImage(trackImageUri: String) {
    val context = LocalContext.current
    val imageUri = Uri.parse(trackImageUri)
    Log.d(TAG, imageUri.toString())

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context).data(imageUri).size(Size.ORIGINAL).build()
    )

    Image(
        painter = painter,
        contentDescription = "Track Image",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )

}

@Preview(showBackground = true)
@Composable
fun TrackScreenPreview() {
    val navController = rememberNavController()
    val trackRepository = TrackRepository(TrackDatabase(LocalContext.current))
    RouteCanvasTheme {
        TrackScreen(2, trackRepository, { navController.popBackStack() })
    }
}