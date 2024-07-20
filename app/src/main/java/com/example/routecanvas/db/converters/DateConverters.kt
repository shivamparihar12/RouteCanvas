package com.example.routecanvas.db.converters

import androidx.room.TypeConverter
import java.sql.Timestamp
import java.util.Date

class DateConverters {
    @TypeConverter
    fun fromTimeStamp(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}