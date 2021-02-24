package com.example.todo.data.Repository

import com.example.todo.data.ToDoDao
import com.example.todo.data.models.ToDoData

class ToDoRepository(private val dao: ToDoDao) {
    val getAllData=dao.getAllData()

    suspend fun insertTodo(toDoData: ToDoData){
        dao.insertData(toDoData)
    }

    suspend fun updateData(toDoData: ToDoData){
        dao.updateData(toDoData)
    }

    suspend fun deleteData(toDoData: ToDoData){
        dao.deleteData(toDoData)
    }

    suspend fun deleteAll(){
        dao.deleteAll()
    }
}