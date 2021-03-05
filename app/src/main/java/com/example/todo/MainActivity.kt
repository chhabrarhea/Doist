package com.example.todo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.findNavController
import com.example.todo.data.models.ToDoData

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(intent.getParcelableExtra<ToDoData>(NewAppWidget().EXTRA_LABEL)!=null){
            val obj=intent.getParcelableExtra<ToDoData>(NewAppWidget().EXTRA_LABEL)

            if (obj!!.description!=""){
                val bundle=Bundle()
                bundle.putParcelable("currentItem",obj)
            findNavController(R.id.navHostFragment)
                .navigate(R.id.updateFragment,bundle)}
            else{
                val bundle=Bundle()
                bundle.putParcelable("currentList",obj)
                findNavController(R.id.navHostFragment)
                    .navigate(R.id.updateCheckListFragment,bundle)}
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.navHostFragment)
        return navController.navigateUp() || super.onSupportNavigateUp()

    }
}