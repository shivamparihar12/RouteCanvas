package com.example.routecanvas.db.converters

import androidx.room.TypeConverter
import com.example.routecanvas.model.LocationPoints
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocationPointsConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromLocationPoints(value: String?): List<LocationPoints> {
        val listType = object : TypeToken<List<LocationPoints>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toLocationPointsList(list: List<LocationPoints>): String {
        return gson.toJson(list)
    }
}