package com.example.todo.fragments.checklist

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.R
import com.example.todo.data.TodoViewModel
import com.example.todo.data.models.CheckListTask
import com.example.todo.data.models.ToDoData
import com.example.todo.databinding.FragmentUpdateCheckListBinding
import com.example.todo.fragments.SharedViewModel
import kotlin.collections.ArrayList


class UpdateCheckListFragment : Fragment(),View.OnTouchListener{
    private lateinit var binding: FragmentUpdateCheckListBinding
    private lateinit var args: ToDoData
    private lateinit var adapter: CheckListAdapter
    private val todoViewModel: TodoViewModel by viewModels()
    private val viewModel: SharedViewModel by viewModels()
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentUpdateCheckListBinding.inflate(inflater, container, false)
        args = requireArguments().getParcelable("currentList")!!
        binding.args = args
        adapter = CheckListAdapter(ArrayList(args.checklist)) { itemClicked() }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.task.setOnTouchListener(this)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbar)
        binding.prioritiesSpinner.onSpinnerItemSelectedListener = viewModel.initializeSpinnerForCheckList(requireContext())
        binding.reminderLayout.setOnClickListener {
            val alertDialog=AlertDialog.Builder(requireContext())
            alertDialog.setTitle("Cancel Reminder?")
            alertDialog.setPositiveButton("Yes"){_,_->run{
               binding.reminderLayout.visibility=View.GONE
                todoViewModel.deleteReminder(args.id)
            }}
            alertDialog.setNegativeButton("No",null)
            alertDialog.create().show()
        }
        return binding.root
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
                    binding.task.setText("")
                    val task = CheckListTask(binding.task.text.toString(), false)
                    adapter.addItem(task)
                }
                return true
            }
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_checklist_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> saveData()
            R.id.menu_delete->deleteData()
            R.id.reminder->viewModel.setReminder(requireContext(),binding.reminderLayout)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteData() {
        todoViewModel.deleteData(args,requireContext())
        Toast.makeText(requireContext(),"Successfully deleted ${binding.titleEt.text}",Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_updateCheckListFragment_to_listFragment)
    }
    private fun saveData() {
        if (binding.titleEt.text.toString().isEmpty() || adapter.listTask.size == 0)
            Toast.makeText(requireContext(), "Some required fields are empty!", Toast.LENGTH_SHORT)
                .show()
        else if(viewModel.date!=null && viewModel.date!!.timeInMillis<System.currentTimeMillis()){
            Toast.makeText(requireContext(), "Set a later time for reminder!", Toast.LENGTH_SHORT)
                .show()
        }
        else {
            val todo= ToDoData(
                    args.id,
                    binding.titleEt.text.toString(),
                    viewModel.parsePriority(binding.prioritiesSpinner.selectedItem.toString()),
                    "",
                    args.date,
                    "",
                    "",
                    "",
                    adapter.listTask,"",viewModel.dateString)
                todoViewModel.updateData(todo,requireContext(),viewModel.date)
            Toast.makeText(requireContext(),"Successfully updated ${binding.titleEt.text}!",Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateCheckListFragment_to_listFragment)
        }

    }


}