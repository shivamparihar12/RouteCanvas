package com.example.routecanvas.ui.composables

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asFlow
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.routecanvas.db.TrackDatabase
import com.example.routecanvas.repository.TrackRepository
import com.example.routecanvas.ui.theme.RouteCanvasTheme
import com.example.routecanvas.viewmodel.TrackViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

const val TAG = "HomeScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToAbout: () -> Unit,
    navigateToRunning: () -> Unit,
    trackRepository: TrackRepository,
    navigateToTrackScreen: (Int) -> Unit
) {
    val gridState = rememberLazyGridState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var showMenu by remember { mutableStateOf(false) }
    val trackViewModel = TrackViewModel(trackRepository)
    val trackList by trackViewModel.getAllTracks().asFlow().collectAsState(initial = emptyList())
    Log.d(TAG, "Track table  ${trackList.size}")

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                text = "all runs", color = MaterialTheme.colorScheme.primary
            )
        }, actions = {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(text = {
                    Text(
                        text = "Run",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }, leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Start running",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }, onClick = { navigateToRunning() })
                DropdownMenuItem(text = {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }, leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "About Page",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }, onClick = { navigateToAbout() })
            }
        }, scrollBehavior = scrollBehavior
        )
    }) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            RequestStoragePermission {
                if (trackList.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 56.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nothing To see here.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Let's Start a run.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(trackList.size) { index: Int ->
                            TrackCard(
                                trackImageUri = trackList[index].trackImageUri,
                                navigateToTrackScreen,
                                index
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackCard(trackImageUri: String, navigateToTrackScreen: (Int) -> Unit, id: Int) {
    val context = LocalContext.current
    val imageUri = Uri.parse(trackImageUri)
    Log.d(TAG, imageUri.toString())

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context).data(imageUri)
            .size(coil.size.Size.ORIGINAL) // This tells the image loader to respect the original image size
//            .crossfade(true)
            .build()
    )

    Box(modifier = Modifier
        .aspectRatio(1f)
        .fillMaxWidth()
        .padding(4.dp)
        .background(Color.White, RoundedCornerShape(8.dp))
        .clickable { navigateToTrackScreen(id) }) {
        Image(
            painter = painter,
            contentDescription = "Track Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Inside
        )
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestStoragePermission(onPermissionGranted: @Composable () -> Unit) {
    val storagePermissionState =
        rememberPermissionState(android.Manifest.permission.READ_EXTERNAL_STORAGE)

    LaunchedEffect(key1 = Unit) {
        if (!storagePermissionState.status.isGranted) {
            storagePermissionState.launchPermissionRequest()
        }
    }

    if (storagePermissionState.status.isGranted) {
        onPermissionGranted()
    } else {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Permission required to access images",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(onClick = { storagePermissionState.launchPermissionRequest() }) {
                    Text(
                        text = "Grant Permission",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    RouteCanvasTheme {
        val trackRepository = TrackRepository(TrackDatabase(LocalContext.current))
        val navController = rememberNavController()
        HomeScreen(navigateToAbout = { navController.navigate(Screens.About) },
            navigateToRunning = { navController.navigate(Screens.RunningScreen) },
            trackRepository = trackRepository,
            navigateToTrackScreen = { navController.navigate(Screens.TrackScreen) })
    }
}