package com.example.routecanvas

import android.content.Context
import com.example.routecanvas.viewmodel.LocationViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationSettingsManager(private val context: Context) {
    private val _locationSettingsState =
        MutableStateFlow<LocationSettingsState>(LocationSettingsState.Unknown)
    val locationSettingsState: StateFlow<LocationSettingsState> =
        _locationSettingsState

    fun checkLocationSettings() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(MyLocationService.locationRequest)
        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            _locationSettingsState.value = LocationSettingsState.Satisfied
        }

        task.addOnFailureListener { exception ->
            _locationSettingsState.value = if (exception is ResolvableApiException) {
                LocationSettingsState.Resolvable(exception)
            } else {
                LocationSettingsState.Inadequate
            }
        }
    }
}

sealed class LocationSettingsState {
    object Unknown : LocationSettingsState()
    object Satisfied : LocationSettingsState()
    data class Resolvable(val exception: ResolvableApiException) : LocationSettingsState()
    object Inadequate : LocationSettingsState()
}