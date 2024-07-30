package com.example.routecanvas.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.net.Uri
import android.os.IBinder
import androidx.compose.runtime.MutableState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.routecanvas.LocationSettingsManager
import com.example.routecanvas.LocationSettingsState
import com.example.routecanvas.MyLocationService
import com.example.routecanvas.db.LocationEntity
import com.example.routecanvas.repository.TrackRepository
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.sql.Time
import java.util.Calendar
import java.util.Date


class LocationViewModel(
    application: Application,
    val trackRepository: TrackRepository,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val _locationList = savedStateHandle.getStateFlow("locationList", emptyList<Location>())
    private val locationList = _locationList

    private var _locationService: WeakReference<MyLocationService>? = null
    private var _serviceBounded = MutableStateFlow(false)
    val serviceBounded = _serviceBounded.asStateFlow()

    private val locationSettingsManager =
        LocationSettingsManager(getApplication<Application>().applicationContext)

    val locationSettingsState: StateFlow<LocationSettingsState> =
        locationSettingsManager.locationSettingsState

    fun checkLocationSetting() {
        locationSettingsManager.checkLocationSettings()
    }

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

    init {
        bindLocationService()
    }

    fun bindLocationService() {
        if (!serviceBounded.value) Intent(
            getApplication(),
            MyLocationService::class.java
        ).also { intent: Intent ->
            getApplication<Application>().bindService(
                intent, serviceConnection, Context.BIND_AUTO_CREATE
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

    fun addLocation(location: Location) {
        savedStateHandle["locationList"] = _locationList.value + location
    }

    fun startGettingLocationUpdate() {
        _locationService?.get()?.startLocationUpdate()
        viewModelScope.launch {
            _locationService?.get()?.locationStateFlow?.collect { location ->
                location?.let {
                    addLocation(it)
                }
            }
        }
    }

    fun stopLocationUpdate() = _locationService?.get()?.stopLocationUpdates()

    fun getLocationList() = locationList

    fun clearLocationList() =
        viewModelScope.launch { savedStateHandle["locationList"] = emptyList<Location>() }

    override fun onCleared() {
        super.onCleared()
        unBindLocationService()
        clearLocationList()
    }

    fun saveTrack(
        uri: Uri, startTime: MutableState<Long>, endTime: MutableState<Long>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            LocationEntity(
                date = Calendar.getInstance().time,
                timeLapsed = endTime.value - startTime.value,
                trackImageUri = uri.path.toString()
            ).let { trackRepository.saveTrack(it) }
        }
    }


}