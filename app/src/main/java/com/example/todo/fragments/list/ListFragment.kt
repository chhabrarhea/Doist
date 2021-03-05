package com.example.todo.fragments.list

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.todo.R
import com.example.todo.utils.FabAnimation
import com.example.todo.data.TodoViewModel
import com.example.todo.databinding.FragmentListBinding
import com.example.todo.fragments.SharedViewModel
import com.google.android.material.snackbar.Snackbar


class ListFragment : Fragment(),SearchView.OnQueryTextListener {
    private val adapter:ListAdapter by lazy { ListAdapter() }
    private val todoViewModel:TodoViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels()
    lateinit var view: FragmentListBinding
    private lateinit var anim:Animatable
    var  isFabMenuOpen=false
    var staggered=true
    private lateinit var gridView:MenuItem

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        view=FragmentListBinding.inflate(inflater, container, false)
        setRecyclerView()
        view.lifecycleOwner=this
        view.sharedVM=sharedViewModel
        setHasOptionsMenu(true)
        (activity as AppCompatActivity?)!!.setSupportActionBar(view.toolbar)

         todoViewModel.getAllData.observe(viewLifecycleOwner, {
             adapter.setData(it)
             sharedViewModel.checkListIsEmpty(it)
         })
         FabAnimation.init(view.addNote)
         FabAnimation.init(view.addList)
         view.menuButton.setOnClickListener{ toggleMenu()}


        return view.root
    }

    private fun toggleMenu() {
        FabAnimation.rotate(view.menuButton,!isFabMenuOpen)
         if(!isFabMenuOpen){
            FabAnimation.fabOpen(view.addList)
            FabAnimation.fabOpen(view.addNote)
            isFabMenuOpen=true

         }
        else {
             FabAnimation.fabClose(view.addList)
             FabAnimation.fabClose(view.addNote)
             isFabMenuOpen=false
         }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)
        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
        gridView=menu.findItem(R.id.menu_orientation)
        if (staggered){
            gridView.icon= ContextCompat.getDrawable(requireContext(), R.drawable.avd_list_to_staggered)

            }
        else
            gridView.icon= ContextCompat.getDrawable(requireContext(), R.drawable.avd_stagerred_to_list)
        anim=gridView.icon as Animatable
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_delete_all->deleteAll()
            R.id.menu_priority_high->adapter.sortList(1)
            R.id.menu_priority_low->adapter.sortList(0)
            R.id.menu_orientation->changeOrientation()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeOrientation() {

        if(staggered){
            gridView.icon= ContextCompat.getDrawable(requireContext(), R.drawable.avd_list_to_staggered)
            view.recyclerView.layoutManager=LinearLayoutManager(requireContext())
            view.recyclerView.adapter=adapter
        }else{
            gridView.icon= ContextCompat.getDrawable(requireContext(), R.drawable.avd_stagerred_to_list)
            view.recyclerView.layoutManager=StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
            view.recyclerView.adapter=adapter
        }
        staggered=!staggered
        anim=gridView.icon as Animatable
        anim.start()
        sharedViewModel.setListOrientation(requireContext(),staggered)
    }
    private fun setRecyclerView(){
        staggered=sharedViewModel.getListOrientation(requireContext())
        view.recyclerView.adapter=adapter
        if (staggered)
            view.recyclerView.layoutManager=StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        else
        view.recyclerView.layoutManager=LinearLayoutManager(activity)
        swipeToDeleteAndUndo()
    }

    private fun deleteAll() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            todoViewModel.deleteAll(requireContext())
            Toast.makeText(
                    requireContext(),
                    "Successfully Removed all data!",
                    Toast.LENGTH_SHORT
            ).show()

        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete Everything?")
        builder.setMessage("Are you sure you want to delete all data?")
        builder.create().show()
    }

    private fun swipeToDeleteAndUndo(){
        val swipeToDelete=object:SwipeToDelete(requireContext()){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem = adapter.dataList[viewHolder.adapterPosition]
                // Delete Item
                todoViewModel.deleteData(deletedItem,requireContext())
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
                val snackBar = Snackbar.make(
                        view.root, "Deleted '${deletedItem.title}'",
                        Snackbar.LENGTH_LONG
                )
                snackBar.setAction("Undo") {
                    todoViewModel.insertData(deletedItem,requireContext())

                }
                snackBar.show()
            } }
        val itemTouchHelper = ItemTouchHelper(swipeToDelete)
        itemTouchHelper.attachToRecyclerView(view.recyclerView)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if(query!=null){
            adapter.filter.filter(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if(newText!=null){
            adapter.filter.filter(newText)
        }
        return true
    }

    override fun onStop() {
        super.onStop()
        for(todo in adapter.alteredList){
            todoViewModel.updateData(todo,requireContext())
        }
    }


}