package com.example.routecanvas.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routecanvas.db.LocationEntity
import com.example.routecanvas.repository.TrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrackViewModel(private val trackRepository: TrackRepository) : ViewModel() {

    private var _specificTrack = MutableStateFlow<LocationEntity?>(null)
    val specificTrack get() = _specificTrack.asStateFlow()

    fun getAllTracks() = trackRepository.getAllTracks()

    fun delTrack(track: LocationEntity) =
        viewModelScope.launch { trackRepository.deleteTrack(track) }

    fun getSpecificTrack(id: Int) = viewModelScope.launch {
        val result = trackRepository.getSpecificTrack(id)
        _specificTrack.value=result

    }
}