package com.example.todo.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "todo_table")
@Parcelize
data class ToDoData(
    @PrimaryKey(autoGenerate = true)
    var id:Int,
    var title:String,
    var priority: Priority,
    var description:String,
    var date: String,
    var reminder:String?=null,
    var checklist: List<CheckListTask>?=null,
    var image:String="",
    var voicenote:String="",
    var url:String="",
    var canvasPath:String=""

): Parcelable
