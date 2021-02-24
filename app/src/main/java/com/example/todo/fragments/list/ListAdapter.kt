package com.example.todo.fragments.list

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.data.models.CheckListTask
import com.example.todo.data.models.ToDoData
import com.example.todo.databinding.RowLayoutBinding
import com.example.todo.databinding.RowLayoutChecklistBinding
import com.example.todo.fragments.checklist.CheckListAdapter
import java.util.*
import kotlin.collections.ArrayList


class ListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(),Filterable{
    var dataList = ArrayList<ToDoData>()
    val filteredList = ArrayList<ToDoData>()
    var lastFilter = ""
    private val note = 0
    private val list = 1
    val alteredList=ArrayList<ToDoData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        var viewHolder:RecyclerView.ViewHolder?=null
        when (viewType) {

            1 -> viewHolder= CheckListViewHolder(
                RowLayoutChecklistBinding.inflate(
                    inflater,
                    parent,
                    false))
            else -> {
                viewHolder= NoteViewHolder(RowLayoutBinding.inflate(inflater, parent, false))
            }}
        return viewHolder
    }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when(holder.itemViewType){
                note->{
                    val viewHolder=holder as NoteViewHolder
                    viewHolder.bind(filteredList[position])
                }
                list->{
                    val viewHolder=holder as CheckListViewHolder
                    viewHolder.bind(filteredList[position])
                }
            }

        }

        override fun getItemCount(): Int {
            return filteredList.size
        }

        fun setData(data: List<ToDoData>) {
            this.dataList.clear()
            this.dataList.addAll(data)
            filter.filter(lastFilter)
//        notifyDataSetChanged updates the views of all items even if only one item has changed. Performance issues.
//        notifyDataSetChanged()
        }

        fun sortList(i: Int) {
            if (i == 1) {
                dataList.sortWith { t: ToDoData, t2: ToDoData ->
                    t.priority.ordinal - t2.priority.ordinal
                }

            } else {
                dataList.sortWith { t: ToDoData, t2: ToDoData ->
                    t2.priority.ordinal - t.priority.ordinal
                }

            }
            filter.filter(lastFilter)
        }

        override fun getItemViewType(position: Int): Int {
            if (filteredList.get(position).description.equals(""))
                return list
            else if (filteredList.get(position).checklist == null)
                return note
            else
                return -1
        }

        override fun getFilter(): Filter {
            return object : Filter() {

                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val list: MutableList<ToDoData> = ArrayList()
                    lastFilter = constraint.toString()
                    if (constraint == null || constraint.isEmpty()) {
                        list.addAll(dataList)
                    } else {
                        val filterPattern: String = constraint.toString().toLowerCase().trim()
                        for (item in dataList) {
                            if (item.title.toLowerCase().contains(filterPattern)) {
                                list.add(item)
                            }
                        }
                    }
                    val results = FilterResults()
                    results.values = list
                    return results
                }

                override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                    filteredList.clear()
//                val diffUtils = DiffUtils(filteredList,list)
//                val result = DiffUtil.calculateDiff(diffUtils)
                    filteredList.addAll(p1!!.values as List<ToDoData>)
                    notifyDataSetChanged()
//                result.dispatchUpdatesTo(this@ListAdapter)
                }

            } }
    inner class CheckListViewHolder(val binding:RowLayoutChecklistBinding):RecyclerView.ViewHolder(binding.root) {
        lateinit var toDoData: ToDoData
        @SuppressLint("ClickableViewAccessibility")
        fun bind(toDoData: ToDoData){
            binding.todo=toDoData
            this.toDoData=toDoData
            val adapter=CheckListAdapter(ArrayList(toDoData.checklist)) { item: CheckListTask ->
                itemClicked(
                    item
                )
            }
            binding.recycle.adapter=adapter
            binding.recycle.layoutManager=LinearLayoutManager(binding.root.context)
        }
        private fun itemClicked(item: CheckListTask) {
            if (!alteredList.contains(toDoData)){
                alteredList.add(toDoData)
            }}



    }}

