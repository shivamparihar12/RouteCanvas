package com.example.routecanvas.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.routecanvas.db.converters.DateConverters
import com.example.routecanvas.db.converters.LocationPointsConverter

@Database(entities = [LocationEntity::class], version = 1)
@TypeConverters(DateConverters::class, LocationPointsConverter::class)
abstract class TrackDatabase : RoomDatabase() {
    abstract fun getLocationDAO(): LocationEntityDAO

    companion object {
        @Volatile
        private var instance: TrackDatabase? = null
        private var LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also {
                instance = it
            }
        }

        private fun createDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, TrackDatabase::class.java, "track_db.db"
        ).build()
    }
}