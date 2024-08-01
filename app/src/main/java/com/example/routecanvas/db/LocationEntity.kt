package com.example.routecanvas.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "track_data")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val lID: Int = 0,
    val date: Date,
    val timeLapsed: Long,
    val trackImageUri: String
)
