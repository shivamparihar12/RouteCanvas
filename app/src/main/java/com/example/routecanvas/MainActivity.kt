package com.example.routecanvas
import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.routecanvas.ui.theme.RouteCanvasTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private var canStartTrackingOP = false
    private lateinit var myNavigationService: MyLocationService
    private var serviceBound: Boolean = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as MyLocationService.LocalBinder
            myNavigationService = binder.getService()
            serviceBound = true

            // Start location updates if permissions are granted
            if (canStartTrackingOP) {
                startLocationUpdates()
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RouteCanvasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        // Request location permissions
        locationPermissionReq.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun onStart() {
        super.onStart()
        // Bind to MyNavigationService
        Intent(this, MyLocationService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        // Check if location services are enabled
        checkLocationSettings()
    }

    override fun onStop() {
        super.onStop()
        // Unbind from MyNavigationService
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }

    private fun startLocationUpdates() {
        // Use CoroutineScope to collect location updates
        CoroutineScope(Dispatchers.IO).launch {
            myNavigationService.locationStateFlow.collect { location ->
                Log.d(TAG, "Location update: ${location?.latitude}, ${location?.longitude}")
                // Handle location updates here as needed
            }
        }
    }

    private fun checkLocationSettings() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Location services are not enabled, prompt user to enable
            Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private val locationPermissionReq = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Permission granted for ACCESS_FINE_LOCATION
                canStartTrackingOP = true
                if (serviceBound) {
                    startLocationUpdates()
                }
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Request fine location as needed
                fineLocationPermissionReq.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                // No permission granted
                Toast.makeText(
                    this,
                    "Permission to access location not granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private val fineLocationPermissionReq =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted for ACCESS_FINE_LOCATION
                canStartTrackingOP = true
                if (serviceBound) {
                    startLocationUpdates()
                }
            } else {
                Toast.makeText(
                    this,
                    "Permission to access fine location not granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RouteCanvasTheme {
        Greeting("Android")
    }
}
