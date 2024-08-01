package com.example.routecanvas.repository

import com.example.routecanvas.db.LocationEntity
import com.example.routecanvas.db.TrackDatabase

class TrackRepository(private val db: TrackDatabase) {

     fun getAllTracks() = db.getLocationDAO().getAllTracks()

     fun deleteTrack(track: LocationEntity) = db.getLocationDAO().deleteTrack(track)

    suspend fun saveTrack(track: LocationEntity) = db.getLocationDAO().saveTrack(track)

    suspend fun getSpecificTrack(id:Int) =  db.getLocationDAO().getSpecificTrack(id)
}