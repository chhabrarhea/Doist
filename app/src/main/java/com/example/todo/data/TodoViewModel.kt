package com.example.todo.data

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.todo.NewAppWidget
import com.example.todo.data.Repository.ToDoRepository
import com.example.todo.data.models.CheckListTask
import com.example.todo.data.models.Priority
import com.example.todo.data.models.ToDoData
import kotlinx.coroutines.launch

class TodoViewModel(application: Application):AndroidViewModel(application) {
    private val toDoDao=ToDoDatabase.getDatabase(application).toDoDao()
    private val repository:ToDoRepository

    val getAllData:LiveData<List<ToDoData>>

    init{
        repository=ToDoRepository(toDoDao)
        getAllData=repository.getAllData
    }
    fun insertData(toDoData: ToDoData,context:Context){
        viewModelScope.launch {
            repository.insertTodo(toDoData)
            NewAppWidget().sendRefreshBroadcast(context)
        }
    }

    fun updateData(toDo:ToDoData,context: Context){
        viewModelScope.launch {
            repository.updateData(toDo)
            NewAppWidget().sendRefreshBroadcast(context)
        }

    }
    fun deleteData(toDo: ToDoData,context: Context){
        viewModelScope.launch {
            repository.deleteData(toDo)
            NewAppWidget().sendRefreshBroadcast(context)
        }
    }
    fun deleteAll(context: Context){
        viewModelScope.launch {
            repository.deleteAll()
            NewAppWidget().sendRefreshBroadcast(context)
        }
    }
    fun saveData(title: String,priority:Priority,date:String,list:List<CheckListTask>,context: Context){
        val task= ToDoData(0,title,priority,"",date,"","","",list)
        viewModelScope.launch {
            repository.insertTodo(task)
            NewAppWidget().sendRefreshBroadcast(context)
        }

    }
}