package com.example.todo.fragments.list

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.example.todo.data.models.ToDoData

class DiffUtils (private val oldList: List<ToDoData>,
                 private val newList: List<ToDoData>
): DiffUtil.Callback()  {
    override fun getOldListSize(): Int {
        Log.i("getOldSize","${oldList.size}")
        return oldList.size

    }

    override fun getNewListSize(): Int {
        Log.i("getNewListSize","${newList.size}")
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] === newList[newItemPosition]

    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
                && oldList[oldItemPosition].title == newList[newItemPosition].title
                && oldList[oldItemPosition].description == newList[newItemPosition].description
                && oldList[oldItemPosition].priority == newList[newItemPosition].priority
    }
}