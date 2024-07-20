package com.example.routecanvas.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.routecanvas.model.LocationPoints
import java.util.Date

@Entity(tableName = "track_data")
data class LocationEntity(
    @PrimaryKey val lID: Int,
    val date: Date?,
    val timeLapsed: Long?,
    val locationPointsList: List<LocationPoints>
)
