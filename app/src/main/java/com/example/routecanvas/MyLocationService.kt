package com.example.routecanvas

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class MyLocationService : Service() {


    //    private var job = SupervisorJob()
    private val notificationIdentifier = 123

    //    private var scope = CoroutineScope(Dispatchers.IO + job)
    private val serviceBinder = LocalBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val _locationStateFlow = MutableStateFlow<Location?>(null)
    val locationStateFlow: StateFlow<Location?> = _locationStateFlow.asStateFlow()


    inner class LocalBinder() : Binder() {
        fun getService(): MyLocationService = this@MyLocationService
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "Location Service starting", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Loc SERV starting...")
//        startForegroundServiceHere()
        return START_STICKY
    }

    private fun intializeLocationService() {
        Log.d(TAG, "under init Loc Service")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.MINUTES.toMillis(1)
            fastestInterval = TimeUnit.MINUTES.toMillis(1)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult?.lastLocation != null) {
                    val currentLocation = locationResult.lastLocation
                    Log.d(TAG, "last location ${currentLocation.toString()}")
                    _locationStateFlow.value = currentLocation
                } else Log.d(TAG, "this location is null")

            }
        }
        startLocationUpdate()
    }

    override fun onCreate() {
        Log.d(TAG, "inside onCreate()")
        intializeLocationService()
        startForegroundServiceHere()
//        startForeground(notificationIdentifier, createNotification()) TODO client will start service expose as fun to do that
        super.onCreate()
    }

     fun startForegroundServiceHere() {
        val notification = createNotification()
        startForeground(notificationIdentifier, notification)
        startLocationUpdate()
    }

    override fun onDestroy() {
        Toast.makeText(this, "Location Service done", Toast.LENGTH_SHORT).show()
        stopLocationUpdates()
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder {
        return serviceBinder
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun createNotification(): Notification {
        val channelId = "location_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Location Service", NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId).setContentTitle("Location Service")
            .setContentText("Tracking location...").setSmallIcon(R.drawable.ic_launcher_background)
            .build()
    }

    companion object {
        private const val TAG = "MyNavigationService"

//        private const val PACKAGE_NAME = "com.example.routecanvas"
//
//        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
//            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"
//
//        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"

    }

}