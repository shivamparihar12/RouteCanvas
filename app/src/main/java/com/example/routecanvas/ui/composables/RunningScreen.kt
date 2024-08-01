package com.example.routecanvas.ui.composables

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Resources.Theme
import android.graphics.Bitmap
import android.location.Location
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routecanvas.LocationSettingsState
import com.example.routecanvas.db.TrackDatabase
import com.example.routecanvas.model.LocationPoints
import com.example.routecanvas.repository.TrackRepository
import com.example.routecanvas.ui.theme.Blue
import com.example.routecanvas.ui.theme.RouteCanvasTheme
import com.example.routecanvas.viewmodel.LocationViewModel
import com.example.routecanvas.viewmodel.LocationViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume


/*think about how to transition after saving and then displaying that track is saved
 for now just show a Toast
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RunningScreen(application: Application, trackRepository: TrackRepository) {
    val locationViewModel: LocationViewModel = viewModel(
        factory = LocationViewModelFactory(
            application = application,
            trackRepository = trackRepository,
            owner = LocalSavedStateRegistryOwner.current
        )
    )
    val TAG = "RunningScreen Composable"
    var isPermissionsAndGpsReady by rememberSaveable { mutableStateOf(false) }
    if (!isPermissionsAndGpsReady) {
        LocationPermissionAndGpsCheck(locationViewModel) {
            isPermissionsAndGpsReady = true
        }
    } else {


        val startTrackTime = rememberSaveable { mutableLongStateOf(0L) }
        val endTrackTime = rememberSaveable { mutableLongStateOf(0L) }
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
        if (!locationViewModel.serviceBounded.collectAsState().value) return Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Pls Restart App",
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        }
        // apparently this should stay in my viewmodel. But F*** yeah here we go.
        fun saveBitmapFromComposable() {
            if (writeStorageAccessState.allPermissionsGranted) {
                coroutineScope.launch {
                    val bitmap = graphicsLayer.toImageBitmap()
                    val uri = bitmap.asAndroidBitmap()
                        .saveToDisk(context)// learnt new thing kotlin , extension fun cool!!
                    Log.d(TAG, uri.toString())
                    saveToRoomDB(context, locationViewModel, uri, startTrackTime, endTrackTime)
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
        Scaffold(modifier = Modifier.background(Color.White)) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ) {
                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .drawWithCache {
                        onDrawWithContent {
                            graphicsLayer.record {
                                this@onDrawWithContent.drawContent()
                            }
                            drawLayer(graphicsLayer = graphicsLayer)
                        }
                    }) {
                    TrackingPath(
                        pointsList = locationViewModel.getLocationList()
                            .collectAsStateWithLifecycle(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly

                ) {
                    Button(onClick = {
                        locationViewModel.startGettingLocationUpdate()
                        Log.d(TAG, "Location updates started...")
                        startTrackTime.longValue = System.currentTimeMillis()
                    }, modifier = Modifier.padding(10.dp)) {

                        Text(text = "Start", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = {
                        locationViewModel.stopLocationUpdate()
//                    endTrackTime.longValue = System.currentTimeMillis()
                        Log.d(TAG, "Location Updates Stopped...")
                    }, modifier = Modifier.padding(10.dp)) {
                        Text(text = "Stop", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = {
                            saveBitmapFromComposable()
                            endTrackTime.longValue = System.currentTimeMillis()
                        }, modifier = Modifier.padding(10.dp)
                    ) {

                        Text(text = "Save", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

    }
}

private fun saveToRoomDB(
    context: Context,
    locationViewModel: LocationViewModel,
    uri: Uri,
    startTrackTime: MutableState<Long>,
    endTrackTime: MutableState<Long>
) {
    // thats where im calling save in to room db
    locationViewModel.saveTrack(uri, startTrackTime, endTrackTime)
    Toast.makeText(context, "Track Saved", Toast.LENGTH_SHORT).show()
}

private fun Bitmap.saveToDisk(context: Context): Uri {
    val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "track-${System.currentTimeMillis()}.png"
    )
    Log.d("$TAG\tfile path\t", file.path)
    file.writeBitmap(this, Bitmap.CompressFormat.PNG, 100)
    return file.path.toUri()

//    return scanFilePath(context, file.path) ?: throw Exception("File could not be saved")
}

private suspend fun scanFilePath(context: Context, path: String): Uri? {
    return suspendCancellableCoroutine {
        MediaScannerConnection.scanFile(
            context, arrayOf(path), arrayOf("image/png")
        ) { _, scannedUri ->
            if (scannedUri == null) {
                it.cancel(Exception("File $path could not be found"))
            } else it.resume(scannedUri)
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
            .background(Color.White)
    ) {
        Log.d("MainAC", "size of points list ${pointsList.value.size}")

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
            color = Blue, // Defined in theme
            style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            blendMode = androidx.compose.ui.graphics.BlendMode.SrcOver
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
            LocationPoints(
                pointsList.value.first().longitude, pointsList.value.last().latitude
            ),
            size,
            minLat - latPadding,
            maxLat + latPadding,
            minLon - lonPadding,
            maxLon + lonPadding
        )
//        firstPoint.let {
//            drawCircle(
//                color = Color.Green, radius = 10f, center = it
//            )
//            drawCircle(
//                color = Color.White, radius = 8f, center = it
//            )
//        }
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
fun LocationPermissionAndGpsCheck(
    locationViewModel: LocationViewModel, onPermissionAndGpsReady: () -> Unit
) {
    val context = LocalContext.current
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val locationSettingsState by locationViewModel.locationSettingsState.collectAsState()

    val gpsSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            locationViewModel.checkLocationSetting()
        }
    }

    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (!locationPermissionState.allPermissionsGranted) {
            locationPermissionState.launchMultiplePermissionRequest()
        } else {
            locationViewModel.checkLocationSetting()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            locationPermissionState.allPermissionsGranted && locationSettingsState == LocationSettingsState.Satisfied -> {
                LaunchedEffect(Unit) {
                    onPermissionAndGpsReady()
                }
            }

            locationPermissionState.shouldShowRationale -> {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Location permission is required for this feature to work.",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { locationPermissionState.launchMultiplePermissionRequest() }) {
                        Text(
                            text = "Request Permission", style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            !locationPermissionState.allPermissionsGranted -> {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Location permission has been denied. Please enable it in app settings.",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { openAppSettings(context) }) {
                        Text(text = "Open Settings", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            locationSettingsState is LocationSettingsState.Resolvable -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "GPS is needed for this service, Pls enable GPS.",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        val intentSenderRequest =
                            IntentSenderRequest.Builder((locationSettingsState as LocationSettingsState.Resolvable).exception.resolution)
                                .build()
                        gpsSettingsLauncher.launch(intentSenderRequest)
                    }) {
                        Text("Adjust GPS Settings", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            locationSettingsState == LocationSettingsState.Inadequate -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        "Your device's GPS capabilities are inadequate for this app. Please use a device with better GPS support.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.fromParts("package", context.packageName, null)
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
    val trackRepository = TrackRepository(TrackDatabase(LocalContext.current))
    val application = Application()
    RouteCanvasTheme {
        RunningScreen(application, trackRepository)
    }
}