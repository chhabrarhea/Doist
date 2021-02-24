package com.example.todo.fragments.checklist

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.example.todo.R
import com.example.todo.data.models.Priority

class CheckListViewModel(application: Application):AndroidViewModel(application) {

    fun parsePriority(priority: String): Priority {
        Log.i("prior", priority)
        return when (priority) {
            "High Priority" -> Priority.HIGH
            "Medium Priority" -> Priority.MEDIUM
            "Low Priority" -> Priority.LOW
            else -> Priority.LOW

        }

    }
    val listener: AdapterView.OnItemSelectedListener = object :
        AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            when (position) {
                0 -> {
                    Log.i("itemSelected","${view?.id}  ")
                    val text = (view as TextView?)
                    text?.setTextAppearance(R.style.textAppearance)
                    text?.setTextColor(ContextCompat.getColor(application, R.color.high))
                }
                1 -> {
                    Log.i("itemSelected","${view?.id}  ")
                    val text = (parent?.getChildAt(0) as TextView?)
                    text?.setTextAppearance(R.style.textAppearance)
                    text?.setTextColor(ContextCompat.getColor(application, R.color.medium))
                }
                2 -> {
                    val text = (parent?.getChildAt(0) as TextView?)
                    text?.setTextAppearance(R.style.textAppearance)
                    text?.setTextColor(ContextCompat.getColor(application, R.color.low))
                }
            }
        }
    }


}