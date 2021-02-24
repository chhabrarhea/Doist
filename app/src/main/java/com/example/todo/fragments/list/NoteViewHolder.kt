package com.example.todo.fragments.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.data.models.ToDoData
import com.example.todo.databinding.RowLayoutBinding

class NoteViewHolder(val binding: RowLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(toDoData: ToDoData) {

//            binding.titleTxt.text=toDoData.title
//            binding.descriptionTxt.text=toDoData.description
//            when(toDoData.priority){
//                Priority.HIGH->binding.priorityIndicator.setCardBackgroundColor(Color.RED)
//                Priority.MEDIUM->binding.priorityIndicator.setCardBackgroundColor(Color.YELLOW)
//                Priority.LOW->binding.priorityIndicator.setCardBackgroundColor(Color.GREEN)
//            }
        binding.todo = toDoData
//            binding.rowBackground.setOnClickListener {
//                val action=ListFragmentDirections.actionListFragmentToUpdateFragment(toDoData)
//                binding.root.findNavController().navigate(action)
//            }


    }


    companion object {
        fun from(parent: ViewGroup): NoteViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = RowLayoutBinding.inflate(layoutInflater, parent, false)
            return NoteViewHolder(binding)
        }
    }

}