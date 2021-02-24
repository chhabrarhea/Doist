package com.example.todo.data

import androidx.room.TypeConverter
import com.example.todo.data.models.Priority

class PriorityConvertor {
    @TypeConverter
    fun fromObjectToString(priority: Priority):String{
        return priority.name
    }
    @TypeConverter
    fun fromStringToObject(priority:String): Priority {
        return Priority.valueOf(priority)
    }
}