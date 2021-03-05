package com.example.todo.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class Priority:Parcelable {
    HIGH,MEDIUM,LOW
}