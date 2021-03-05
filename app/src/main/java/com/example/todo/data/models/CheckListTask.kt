package com.example.todo.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
class CheckListTask(var task:String,var done:Boolean):Parcelable