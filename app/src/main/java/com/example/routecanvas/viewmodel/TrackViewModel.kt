package com.example.routecanvas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routecanvas.db.LocationEntity
import com.example.routecanvas.repository.TrackRepository
import kotlinx.coroutines.launch

class TrackViewModel(private val trackRepository: TrackRepository) : ViewModel() {

    fun delTrack(track: LocationEntity) =
        viewModelScope.launch { trackRepository.deleteTrack(track) }
}