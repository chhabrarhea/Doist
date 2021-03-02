package com.example.todo.fragments.checklist

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class UpdateCheckListFragment : Fragment(),View.OnTouchListener{
    lateinit var binding: FragmentUpdateCheckListBinding
    lateinit var args: ToDoData
    lateinit var adapter: CheckListAdapter
    private val todoViewModel: TodoViewModel by viewModels()
    private val viewModel: SharedViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentUpdateCheckListBinding.inflate(inflater, container, false)
        args = requireArguments().getParcelable("currentList")!!
        binding.args = args
        adapter = CheckListAdapter(ArrayList(args.checklist)) { item: CheckListTask -> itemClicked(item) }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.task.setOnTouchListener(this)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbar)
        binding.prioritiesSpinner.onSpinnerItemSelectedListener = viewModel.initializeSpinnerForCheckList(requireContext())
        return binding.root
    }

    private fun itemClicked(item: CheckListTask) {

    }

    override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
        if (event?.getAction() == MotionEvent.ACTION_UP) {
            if (event.getRawX() >= (binding.task.getRight() - binding.task.getCompoundDrawables()[2].getBounds()
                    .width())
            ) {
                if (binding.task.text.toString().isEmpty()) {
                    binding.nameLayout.isErrorEnabled = true
                    binding.nameLayout.error = "Cannot be Empty"
                } else {
                    binding.nameLayout.isErrorEnabled = false
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
        else {
            todoViewModel.updateData(
                ToDoData(
                    args.id,
                    binding.titleEt.text.toString(),
                    viewModel.parsePriority(binding.prioritiesSpinner.selectedItem.toString()),
                    "",
                    args.date,
                    "",
                    "",
                    "",
                    adapter.listTask,""
                ),requireContext()
            )
            Toast.makeText(requireContext(),"Successfully updated ${binding.titleEt.text}!",Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateCheckListFragment_to_listFragment)
        }

    }


}