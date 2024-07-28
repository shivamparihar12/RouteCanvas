package com.example.routecanvas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routecanvas.db.LocationEntity
import com.example.routecanvas.repository.TrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrackViewModel(private val trackRepository: TrackRepository) : ViewModel() {

    fun getallTracks() = viewModelScope.launch(Dispatchers.IO) {
        trackRepository.getAllTracks()
    }

    fun delTrack(track: LocationEntity) =
        viewModelScope.launch { trackRepository.deleteTrack(track) }
}