package com.example.todo.fragments.checklist

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.R
import com.example.todo.data.TodoViewModel
import com.example.todo.data.models.CheckListTask
import com.example.todo.data.models.ToDoData
import com.example.todo.databinding.FragmentAddChecklistBinding
import com.example.todo.fragments.SharedViewModel
import com.example.todo.utils.Reminders

class AddChecklistFragment : Fragment(), View.OnTouchListener {
    private lateinit var binding: FragmentAddChecklistBinding
    private lateinit var adapter: CheckListAdapter
    private var date=""
    private val todoViewModel: TodoViewModel by viewModels()
    private val viewModel: SharedViewModel by viewModels()
    private lateinit var reminders: Reminders



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddChecklistBinding.inflate(layoutInflater, container, false)
        reminders= Reminders(requireContext(),binding.reminderLayout)
        initializeUI()
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeUI() {
        adapter = CheckListAdapter(ArrayList()){itemClicked() }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.task.setOnTouchListener(this)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbar)

        binding.prioritiesSpinner.onSpinnerItemSelectedListener=viewModel.initializeSpinnerForCheckList(requireContext(),binding.nameLayout)

        binding.reminderLayout.setOnClickListener {deleteReminder()}
    }

    private fun deleteReminder() {
        val alertDialog= AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Cancel Reminder?")
        alertDialog.setPositiveButton("Yes"){_,_->run{
            binding.reminderLayout.visibility=View.GONE
           reminders.cancelReminder()
        }}
        alertDialog.setNegativeButton("No",null)
        alertDialog.create().show()
    }

    private fun itemClicked() {

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            if (event.rawX >= (binding.task.right - binding.task.compoundDrawables[2].bounds
                    .width())
            ) {
                if (binding.task.text.toString().isEmpty()) {
                    binding.nameLayout.isErrorEnabled = true
                    binding.nameLayout.error = "Cannot be Empty"
                } else {
                    binding.nameLayout.isErrorEnabled = false
                    val task = CheckListTask(binding.task.text.toString(), false)
                    binding.task.setText("")
                    adapter.addItem (task)
                }
                return true
            }
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_checklist_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_save->saveData()
            R.id.reminder->reminders.setReminder()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveData() {
        if(binding.titleEt.text.toString().isEmpty() || adapter.listTask.size==0)
            Toast.makeText(requireContext(),"Some required fields are empty!",Toast.LENGTH_SHORT).show()
        else if(!reminders.validateTime()){
            Toast.makeText(requireContext(), "Set a later time for reminder!", Toast.LENGTH_SHORT)
                .show()
        }
        else {
            val todo= ToDoData(
                0,
                binding.titleEt.text.toString(),
                viewModel.parsePriority(binding.prioritiesSpinner.selectedItem.toString()),
                "",
                date,
                reminders.dateString,
                adapter.listTask)
                todoViewModel.insertData(todo,requireContext(),reminders.date)
            Toast.makeText(requireContext(),"Successfully added ${binding.titleEt.text}!",Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_addChecklistFragment_to_listFragment)
            }
    }




}