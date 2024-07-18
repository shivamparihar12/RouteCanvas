package com.example.routecanvas

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationViewModel(private val locationService: MyLocationService) : ViewModel() {
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
}