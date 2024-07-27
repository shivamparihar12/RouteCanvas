package com.example.routecanvas.ui.composables

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routecanvas.model.LocationPoints
import com.example.routecanvas.ui.theme.RouteCanvasTheme
import com.example.routecanvas.viewmodel.LocationViewModel
import com.example.routecanvas.viewmodel.LocationViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RunningScreen(locationViewModel: LocationViewModel) {
    val TAG = "RunningScreen Composable"
    var isPermissionsAndGpsReady by remember { mutableStateOf(false) }

    if (!isPermissionsAndGpsReady) {
        LocationPermissionAndGpsCheck {
            isPermissionsAndGpsReady = true
        }
    } else {

        LaunchedEffect(Unit) {
            locationViewModel.bindLocationService()
        }

        DisposableEffect(Unit) {
            onDispose {
                locationViewModel.stopLocationUpdate()
                locationViewModel.clearLocationList()
                locationViewModel.unBindLocationService()
            }
        }

        val serviceBounded by locationViewModel.serviceBounded.collectAsState()

        if (!serviceBounded) return Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Pls Restart App",
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        }
//        locationViewModel.startGettingLocationUpdate() // handle this with button

        // canvas -> bitmap requirements
        val context = LocalContext.current
        val graphicsLayer = rememberGraphicsLayer()
        val coroutineScope = rememberCoroutineScope()
        val snackBarHostState = remember { SnackbarHostState() }

        val writeStorageAccessState = rememberMultiplePermissionsState(
            permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                emptyList()
            } else {
                listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        )

        // apparently this should stay in my viewmodel. But F*** yeah here we go.
        fun saveBitmapFromComposable() {
            if (writeStorageAccessState.allPermissionsGranted) {
                coroutineScope.launch {
                    val bitmap = graphicsLayer.toImageBitmap()
                    val uri = bitmap.asAndroidBitmap()
                        .saveToDisk(context)// learnt new thing kotlin , extension fun cool!!
                    saveBitmap(context, uri)
                }
            } else if (writeStorageAccessState.shouldShowRationale) {
                coroutineScope.launch {
                    val result = snackBarHostState.showSnackbar(
                        message = "The storage permission is needed to save your track",
                        actionLabel = "Grant Access"
                    )

                    if (result == SnackbarResult.ActionPerformed) {
                        writeStorageAccessState.launchMultiplePermissionRequest()
                    }
                }
            } else writeStorageAccessState.launchMultiplePermissionRequest()
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .drawWithCache {
//                    val width = this.size.width.toInt()
//                    val height = this.size.height.toInt()
                    onDrawWithContent {
                        graphicsLayer.record {
                            this@onDrawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer = graphicsLayer)
                    }
                }) {
                val pointsList = locationViewModel.getLocationList().collectAsState()
                TrackingPath(
                    pointsList = pointsList, modifier = Modifier.fillMaxWidth()
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    locationViewModel.startGettingLocationUpdate()
                    Log.d(TAG, "Location updates started...")
                }, modifier = Modifier.padding(10.dp)) {
                    Text(text = "Start")
                }
                Button(onClick = {
                    locationViewModel.stopLocationUpdate()
                    Log.d(TAG, "Location Updates Stopped...")
                }, modifier = Modifier.padding(10.dp)) {
                    Text(text = "Stop")
                }
                Button(
                    onClick = { saveBitmapFromComposable() }, modifier = Modifier.padding(10.dp)
                ) {
                    Text(text = "Save")
                }
            }
        }


    }
}

private fun CoroutineScope.saveBitmap(context: Context, uri: Uri) {
    // TODO well you have now  uri, get other stats and save into room db
}

private suspend fun Bitmap.saveToDisk(context: Context): Uri {
    val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "track-${System.currentTimeMillis()}.png"
    )
    file.writeBitmap(this, Bitmap.CompressFormat.PNG, 100)

    return scanFilePath(context, file.path) ?: throw Exception("File could not be saved")
}

private suspend fun scanFilePath(context: Context, path: String): Uri? {
    return suspendCancellableCoroutine {
        MediaScannerConnection.scanFile(
            context, arrayOf(path), arrayOf("image/png")
        ) { _, scanneduri ->
            if (scanneduri == null) {
                it.cancel(Exception("File $path could not be found"))
            } else it.resume(scanneduri)
        }
    }
}

private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    outputStream().use {
        bitmap.compress(format, quality, it)
        it.flush()
    }
}

@Composable
fun TrackingPath(pointsList: State<List<Location>>, modifier: Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Log.d("MainAC", "size of points list $pointsList.size")

        if (pointsList.value.isEmpty()) return@Canvas

        val minLat = pointsList.value.minOf { it.longitude }
        val maxLat = pointsList.value.maxOf { it.longitude }
        val minLon = pointsList.value.minOf { it.latitude }
        val maxLon = pointsList.value.maxOf { it.latitude }

        val latPadding = (maxLat - minLat) * 0.1
        val lonPadding = (maxLon - minLon) * 0.1

        val path = Path()
        pointsList.value.forEachIndexed { index, point ->
            val screenPoint = toScreenCoordinates(
                LocationPoints(point.longitude, point.latitude),
                size,
                minLat - latPadding,
                maxLat + latPadding,
                minLon - lonPadding,
                maxLon + lonPadding
            )
            if (index == 0) path.moveTo(screenPoint.x, screenPoint.y)
            else path.lineTo(screenPoint.x, screenPoint.y)
        }
        drawPath(
            path = path,
            color = Color.Blue,
            style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val lastPoint = toScreenCoordinates(
            LocationPoints(pointsList.value.last().longitude, pointsList.value.last().latitude),
            size,
            minLat - latPadding,
            maxLat + latPadding,
            minLon - lonPadding,
            maxLon + lonPadding
        )

        val firstPoint = toScreenCoordinates(
            LocationPoints(pointsList.value.first().longitude, pointsList.value.last().latitude),
            size,
            minLat - latPadding,
            maxLat + latPadding,
            minLon - lonPadding,
            maxLon + lonPadding
        )
        firstPoint.let {
            drawCircle(
                color = Color.Green, radius = 10f, center = it
            )
            drawCircle(
                color = Color.White, radius = 8f, center = it
            )
        }
        lastPoint.let {
            drawCircle(
                color = Color.Red, radius = 10f, center = it
            )
            drawCircle(
                color = Color.White, radius = 8f, center = it
            )
        }


    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionAndGpsCheck(onPermissionAndGpsReady: () -> Unit) {
    val context = LocalContext.current
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    var isGPSEnabled by remember { mutableStateOf(isGpsEnabled(context)) }
    LaunchedEffect(Unit) {
        if (!locationPermissionState.allPermissionsGranted) {
            locationPermissionState.launchMultiplePermissionRequest()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        when {
            locationPermissionState.allPermissionsGranted && isGPSEnabled -> {
                LaunchedEffect(Unit) {
                    onPermissionAndGpsReady()
                }
            }

            locationPermissionState.shouldShowRationale -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(text = "Location permission is required for this feature to work.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { locationPermissionState.launchMultiplePermissionRequest() }) {
                        Text(text = "Request Permission")
                    }
                }
            }

            !locationPermissionState.allPermissionsGranted -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text("Location permission has been denied. Please enable it in app settings.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { openAppSettings(context) }) {
                        Text(text = "Open Setting")
                    }
                }
            }

            !isGPSEnabled -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text("GPS is disabled. Please enable it to use this feature.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        openGpsSettings(context)
                        isGPSEnabled = isGpsEnabled(context)
                    }) {
                        Text("Enable GPS")
                    }
                }
            }
        }
    }
}

fun openGpsSettings(context: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    context.startActivity(intent)
}

fun isGpsEnabled(context: Context): Boolean {
    val locationManager = ContextCompat.getSystemService(context, LocationManager::class.java)
    return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
}

fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = android.net.Uri.fromParts("package", context.packageName, null)
    context.startActivity(intent)
}

private fun toScreenCoordinates(
    point: LocationPoints,
    canvasSize: Size,
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double
): Offset {
    val p = ((point.latitude - minLon) / (maxLon - minLon) * canvasSize.width).toFloat()
    val q = ((maxLat - point.longitude) / (maxLat - minLat) * canvasSize.height).toFloat()
    return Offset(p, q)
}


@Preview(showBackground = true)
@Composable
fun RunningScreenPreview() {
    val locationViewModel: LocationViewModel =
        viewModel(factory = LocationViewModelFactory(Application()))
    RouteCanvasTheme {
        RunningScreen(locationViewModel)
    }
}