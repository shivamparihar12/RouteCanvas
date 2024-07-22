package com.example.routecanvas.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.routecanvas.R
import com.example.routecanvas.ui.theme.RouteCanvasTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navigateTo: (route: Screens.TrackScreen) -> Unit) {
//    Button(onClick = { navigateTo(Screens.TrackScreen(id = 12)) }) {
//        Text(text = "go to specific track")
//    }

    val gridState = rememberLazyGridState()
    val topAppBarHeight = 56.dp
    val topAppBarHeightPx = with(LocalDensity.current) { topAppBarHeight.roundToPx().toFloat() }
    val topAppBarOffsetHeightPx = remember { mutableStateOf(0f) }
    var previousIndex by remember { mutableStateOf(0) }
    var previousScrollOffset by remember { mutableStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }

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
                            text = "Item ${index}",
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            TopAppBar(
                title = { Text(text = "RouteCanvas") },
                modifier = Modifier
                    .offset { IntOffset(x = 0, y = topAppBarOffsetHeightPx.value.roundToInt()) },
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
                            onClick = { /*TODO start running > plan out how to proceed from here */ }
                        )

                        DropdownMenuItem(
                            text = { Text(text = "About") },
                            onClick = { /*TODO move to about about compose*/ },
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
    var toolbarOffsetHeightPx = remember { mutableStateOf(0f) }
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
                toolbarOffsetHeightPx.value = toolbarOffset.coerceIn(-topAppBarHeightPx, 0f)
                previousIndex = index
                previousScrollOffset = scrollOffset
            }
    }
    topAppBarOffsetHeightPx.value = toolbarOffsetHeightPx.value
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    RouteCanvasTheme {
        HomeScreen {

        }
    }
}