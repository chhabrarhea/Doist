package com.example.todo.fragments.checklist

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.data.models.CheckListTask
import com.example.todo.data.models.ToDoData
import com.example.todo.databinding.ChecklistBinding
import com.example.todo.databinding.RowLayoutBinding
import com.example.todo.fragments.list.ListAdapter


class CheckListAdapter(val listTask:ArrayList<CheckListTask>,private val clickListener: (CheckListTask) -> Unit): RecyclerView.Adapter<CheckListAdapter.VH>() {

    inner class VH(private val binding: ChecklistBinding):RecyclerView.ViewHolder(binding.root){

        fun bind(task: CheckListTask,clickListener: (CheckListTask) -> Unit){
            binding.itemTv.text=task.task
            if(task.done){
                binding.itemCheckBox.isChecked=true
                binding.itemTv.paintFlags = binding.itemTv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            binding.itemCheckBox.setOnCheckedChangeListener{ buttonView, isChecked ->
                if (isChecked){
                    binding.itemCheckBox.isChecked=true
                    binding.itemTv.paintFlags = binding.itemTv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    task.done=true
                }
                else{
                    binding.itemTv.paintFlags = binding.itemTv.paintFlags and (Paint.STRIKE_THRU_TEXT_FLAG.inv())
                    task.done=false
                }
                clickListener(task)
            }
        }
}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layoutInflater=LayoutInflater.from(parent.context)
        val binding = ChecklistBinding.inflate(layoutInflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
       holder.bind(listTask[position],clickListener)
    }

    override fun getItemCount(): Int {
        return listTask.size
    }
    fun addItem(task: CheckListTask){
        listTask.add(task)
        notifyItemInserted(listTask.size-1)
    }

}