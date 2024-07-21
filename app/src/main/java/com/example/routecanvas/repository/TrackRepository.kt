package com.example.routecanvas.repository

import com.example.routecanvas.db.LocationEntity
import com.example.routecanvas.db.TrackDatabase

class TrackRepository(private val db: TrackDatabase) {

    fun getAllTracks() = db.getLocationDAO().getAllTracks()

    suspend fun deleteTrack(track: LocationEntity) = db.getLocationDAO().deleteTrack(track)
}