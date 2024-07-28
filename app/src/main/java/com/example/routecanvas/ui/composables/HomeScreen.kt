package com.example.routecanvas.ui.composables

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.routecanvas.R
import com.example.routecanvas.db.TrackDatabase
import com.example.routecanvas.repository.TrackRepository
import com.example.routecanvas.ui.theme.RouteCanvasTheme
import com.example.routecanvas.viewmodel.TrackViewModel
import kotlin.math.roundToInt


// we just setuped saving and fetching , now we are about test the functionality

const val TAG = "HomeScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToAbout: () -> Unit,
    navigateToRunning: () -> Unit,
//    trackViewModel: TrackViewModel,
) {
//    Button(onClick = { navigateTo(Screens.TrackScreen(id = 12)) }) {
//        Text(text = "go to specific track")
//    }

    val gridState = rememberLazyGridState()
    val topAppBarHeight = 56.dp
    val topAppBarHeightPx = with(LocalDensity.current) { topAppBarHeight.roundToPx().toFloat() }
    val topAppBarOffsetHeightPx = remember { mutableFloatStateOf(0f) }
    var previousIndex by remember { mutableIntStateOf(0) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }

//    Log.d(TAG, "Track table  ${trackViewModel.getallTracks()}")

    Surface(modifier = Modifier.fillMaxSize()) {
        Box {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = topAppBarHeight)
            ) {
//                item {
//                    Spacer(modifier = Modifier.height(topAppBarHeight))
//                }

                items(100) { index: Int ->
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {

                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "image"
                        )
                        Text(
                            text = "Item $index",
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            TopAppBar(
                title = { Text(text = "RouteCanvas") },
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = 0,
                            y = topAppBarOffsetHeightPx.floatValue.roundToInt()
                        )
                    },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
                    }

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(text = "Run") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Start running"
                                )
                            },
                            onClick = { navigateToRunning() }
                        )

                        DropdownMenuItem(
                            text = { Text(text = "About") },
                            onClick = { navigateToAbout() },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = "About Page"
                                )
                            }
                        )
                    }
                }
            )
        }

    }
    val toolbarOffsetHeightPx = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collect { (index, scrollOffset) ->
                val isScrollingUp =
                    index < previousIndex || (index == previousIndex && scrollOffset < previousScrollOffset)

                val toolbarOffset = when {
                    index == 0 -> -scrollOffset.toFloat()
                    isScrollingUp -> 0f
                    else -> -topAppBarHeightPx
                }
                toolbarOffsetHeightPx.floatValue = toolbarOffset.coerceIn(-topAppBarHeightPx, 0f)
                previousIndex = index
                previousScrollOffset = scrollOffset
            }
    }
    topAppBarOffsetHeightPx.floatValue = toolbarOffsetHeightPx.floatValue
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    RouteCanvasTheme {
        val navController = rememberNavController()
        val trackViewModel = TrackViewModel(TrackRepository(TrackDatabase(LocalContext.current)))
        HomeScreen(
            navigateToAbout = { navController.navigate(Screens.About) },
            navigateToRunning = { navController.navigate(Screens.RunningScreen) },
//            trackViewModel = trackViewModel
        )
    }
}