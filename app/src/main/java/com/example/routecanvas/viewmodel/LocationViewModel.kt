package com.example.routecanvas.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routecanvas.MyLocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val _locationList = MutableStateFlow<List<Location>>(emptyList())
    private val locationList = _locationList.asStateFlow()

    private var _locationService: WeakReference<MyLocationService>? = null
    private var _serviceBounded = MutableStateFlow(false)
    val serviceBounded = _serviceBounded.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as MyLocationService.LocalBinder
            _locationService = WeakReference(binder.getService())
            _serviceBounded.value = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            _serviceBounded.value = false
            _locationService = null
        }
    }

    fun bindLocationService() {
        Intent(getApplication(), MyLocationService::class.java).also { intent: Intent ->
            getApplication<Application>().bindService(
                intent,
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    fun unBindLocationService() {
        if (_serviceBounded.value) {
            getApplication<Application>().unbindService(serviceConnection)
            _serviceBounded.value = false
            _locationService = null
        }
    }


//    init {
//        viewModelScope.launch {
//            _locationService?.get()?.locationStateFlow?.collect { location ->
//                location?.let {
//                    _locationList.value += it
//                }
//            }
//        }
//    }


    fun startGettingLocationUpdate() {
        _locationService?.get()?.startLocationUpdate()
        viewModelScope.launch {
            _locationService?.get()?.locationStateFlow?.collect { location ->
                location?.let {
                    _locationList.value += it
                }
            }
        }
    }

    fun stopLocationUpdate() = _locationService?.get()?.stopLocationUpdates()

    fun getLocationList() = locationList

    fun clearLocationList() = viewModelScope.launch { _locationList.value = emptyList() }
    override fun onCleared() {
        super.onCleared()
        unBindLocationService()
    }
}