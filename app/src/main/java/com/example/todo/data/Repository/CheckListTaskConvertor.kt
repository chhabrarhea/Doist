package com.example.todo.data.Repository

import androidx.room.TypeConverter
import com.example.todo.data.models.CheckListTask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class CheckListTaskConvertor {

    @TypeConverter
     fun fromObjectList(optionValues: List<CheckListTask>?): String? {
        if (optionValues == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CheckListTask>>() {}.type
        return gson.toJson(optionValues, type)
    }

    @TypeConverter // note this annotation
    fun toObjectList(optionValuesString: String?): List<CheckListTask>? {
        if (optionValuesString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CheckListTask>>() {}.type
        return gson.fromJson(optionValuesString, type)
    }
}