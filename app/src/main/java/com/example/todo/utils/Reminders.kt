package com.example.todo.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.*

class Reminders(private val context: Context,private val view:RelativeLayout) {
    var date: Calendar? =null
    var dateString:String?=null

    fun setReminder(){
        val newCalender = Calendar.getInstance();
        val datePicker= DatePickerDialog(
            context,
            { _, year, month, day ->
                run {
                    val newDate = Calendar.getInstance()
                    val newTime = Calendar.getInstance()
                    val time = TimePickerDialog(
                        context,
                        { _, hour, min ->
                            run {
                                newDate.set(year, month, day, hour, min)
                                val tem = Calendar.getInstance()
                                if (newDate.timeInMillis - tem.timeInMillis > 0) {
                                    date=newDate
                                    view.visibility= View.VISIBLE
                                    val tv=view.getChildAt(1) as TextView
                                    val df= SimpleDateFormat("MMM dd, h:mm a",Locale.getDefault())
                                    dateString=df.format(date!!.time)
                                    tv.text=dateString
                                } else Toast.makeText(
                                    context,
                                    "Invalid time",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }, newTime.get(Calendar.HOUR_OF_DAY), newTime.get(Calendar.MINUTE), true
                    )
                    time.show()
                }

            }, newCalender.get(Calendar.YEAR), newCalender.get(Calendar.MONTH), newCalender.get(
                Calendar.DAY_OF_MONTH
            )
        )
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    fun cancelReminder(){
        date=null
        dateString=null
    }

    fun validateTime():Boolean{
        return date==null ||  date!!.timeInMillis>System.currentTimeMillis()
    }

    fun deleteUnsavedReminderDialog(): AlertDialog.Builder{
        val alertDialog= AlertDialog.Builder(context)
        alertDialog.setTitle("Cancel Reminder?")
        alertDialog.setPositiveButton("Yes"){_,_->run{
            view.visibility=View.GONE
            date=null
            dateString=null

        }}
        alertDialog.setNegativeButton("No",null)
        alertDialog.create()
        return alertDialog
    }

}