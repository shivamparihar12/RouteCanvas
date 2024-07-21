package com.example.routecanvas.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routecanvas.MyLocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationViewModel(locationService: MyLocationService) : ViewModel() {
    private val _locationList = MutableStateFlow<List<Location>>(emptyList())
    private val locationList = _locationList.asStateFlow()

    init {
        viewModelScope.launch {
            locationService.locationStateFlow.collect { location ->
                location?.let {
                    _locationList.value += it;
                }

            }
        }
    }

    fun getLocationList(): StateFlow<List<Location>> {
        return locationList
    }
}