package com.example.todo.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.todo.data.models.ToDoData

@Dao
interface ToDoDao {
    @Query("SELECT * FROM todo_table ORDER BY id desc")
    fun getAllData():LiveData<List<ToDoData>>

    @Query("SELECT * FROM todo_table ORDER BY id desc")
    fun getData():List<ToDoData>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertData(toDoData: ToDoData)

    @Update
    suspend fun updateData(toDoData: ToDoData)

    @Delete
    suspend fun deleteData(todo:ToDoData)

    @Query("Delete from todo_table")
    suspend fun deleteAll()
}