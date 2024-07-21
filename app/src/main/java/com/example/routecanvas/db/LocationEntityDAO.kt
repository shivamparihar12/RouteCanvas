package com.example.routecanvas.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query

@Dao
interface LocationEntityDAO {

    @Query("SELECT * FROM track_data")
    fun getAllTracks(): LiveData<List<LocationEntity>>

    @Delete
    fun deleteTrack(track: LocationEntity)

    // TODO add more CRUD functionalities
//    @Query("SELECT * FROM track_data where lID = :id")
//    fun getParticularTrack(id: Int): LocationEntity

//    @Query("")
}