package com.example.todo.data

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.todo.NewAppWidget
import com.example.todo.NotifierAlarm
import com.example.todo.data.Repository.ToDoRepository
import com.example.todo.data.models.CheckListTask
import com.example.todo.data.models.Priority
import com.example.todo.data.models.ToDoData
import kotlinx.coroutines.launch
import java.util.*


class TodoViewModel(application: Application):AndroidViewModel(application) {
    private val toDoDao=ToDoDatabase.getDatabase(application).toDoDao()
    private val repository:ToDoRepository = ToDoRepository(toDoDao)

    val getAllData:LiveData<List<ToDoData>> = repository.getAllData


    fun insertData(toDo: ToDoData, context: Context, date: Calendar?){
        viewModelScope.launch {
           val id= repository.insertTodo(toDo)
            NewAppWidget().sendRefreshBroadcast(context)
            if(id>0 && date!=null){
            toDo.id=id.toInt()
            setReminder(toDo,context,date)}

        }
    }

    fun updateData(toDo: ToDoData, context: Context,date: Calendar?){
        viewModelScope.launch {
            repository.updateData(toDo)
            NewAppWidget().sendRefreshBroadcast(context)
            if(date!=null){
                setReminder(toDo,context,date)}
        }

    }
    fun deleteData(toDo: ToDoData, context: Context){
        viewModelScope.launch {
            repository.deleteData(toDo)
            NewAppWidget().sendRefreshBroadcast(context)
           val intent=Intent(context,NotifierAlarm::class.java)
            val pendingIntent=PendingIntent.getBroadcast(context,toDo.id,intent,PendingIntent.FLAG_CANCEL_CURRENT)
            val alarmManager=context.getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }
    fun deleteAll(context: Context){
        viewModelScope.launch {
            repository.deleteAll()
            NewAppWidget().sendRefreshBroadcast(context)
        }
    }
    fun saveData(
        title: String,
        priority: Priority,
        date: String,
        list: List<CheckListTask>,
        reminder:String?,
        context: Context
    ){
        val task= ToDoData(0, title, priority, "", date, "", "", "", list, "",reminder)
        viewModelScope.launch {
            repository.insertTodo(task)
            NewAppWidget().sendRefreshBroadcast(context)
        }

    }
    fun deleteReminder(id:Int){
        viewModelScope.launch {
        repository.deleteReminder(id)}
    }

    fun setReminder(toDo: ToDoData, context: Context, date: Calendar){
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"))
            calendar.time = date.time
            calendar.set(Calendar.SECOND, 0)
            val intent = Intent(context, NotifierAlarm::class.java)
            val todoBundle = Bundle()
            todoBundle.putParcelable("currentItem", toDo)
            intent.putExtra("currentItem", todoBundle)
            val intent1 = PendingIntent.getBroadcast(
                context,
                toDo.id,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
            val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager?
            alarmManager!!.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, intent1)

    }
}