package com.example.todo.fragments.draw

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.todo.R
import com.example.todo.databinding.FragmentDrawBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class DrawFragment : Fragment(){
private lateinit var binding:FragmentDrawBinding
private lateinit var sheetBehavior:BottomSheetBehavior<CardView>



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding= FragmentDrawBinding.inflate(layoutInflater,container,false)
        sheetBehavior=BottomSheetBehavior.from(binding.bottomSheet)
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
        initStrokeOptions()
        initColorOptions()
        binding.detail.eraser.setOnClickListener{
            binding.canvas.onEraser()
            binding.detail.pen.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.detail.eraser.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.secondaryColor))
        }
        binding.detail.pen.setOnClickListener{
            binding.canvas.onEraser()
            binding.detail.pen.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.secondaryColor))
            binding.detail.eraser.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        binding.detail.delete.setOnClickListener{
            val alertDialog=AlertDialog.Builder(requireContext())
            alertDialog.setTitle("Clear Canvas")
            alertDialog.setMessage("Are you sure you want to delete your progress?")
            alertDialog.setPositiveButton("Yes"){_,_->
                run {
                    binding.canvas.clearBrushes()
                }
            }
            alertDialog.setNegativeButton("No"){_,_->}
        }
        return binding.root
    }

    private fun initStrokeOptions() {
           BrushSizePicker(binding.detail.brush,requireContext(),object:BrushSizePicker.SizeSelected{
               override fun sizeSelected(size: Float) {
                   binding.canvas.setSizeForBrush(size)
               }
           })
    }

    private fun initColorOptions(){
        ColorPicker(binding.detail.palette,object:ColorPicker.ColorPicked{
            override fun colorPicked(color: Int) {
                binding.canvas.setBrushColor(color)
            }
        })
    }





    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.draw_fragment_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.grid->openGridDialog()
            R.id.share->shareDrawing()
            R.id.undo->binding.canvas.undoPath()
            R.id.redo->binding.canvas.redoPath()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun shareDrawing() {
        TODO("Not yet implemented")
    }

    private fun openGridDialog() {
        val dialog=Dialog(requireContext())
        dialog.setContentView(R.layout.grid_dialog)
        dialog.setTitle("Choose Grid:")
        dialog.findViewById<RelativeLayout>(R.id.grid).setOnClickListener { binding.gridBackground.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_grid_texture))
            binding.gridBackground.scaleType=ImageView.ScaleType.FIT_XY
            dialog.dismiss()}
        dialog.findViewById<RelativeLayout>(R.id.ruled).setOnClickListener { binding.gridBackground.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_ruled_texture))
            binding.gridBackground.scaleType=ImageView.ScaleType.FIT_XY
            dialog.dismiss()}
        dialog.findViewById<RelativeLayout>(R.id.isometric).setOnClickListener { binding.gridBackground.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_isometric_texture))
            binding.gridBackground.scaleType=ImageView.ScaleType.CENTER_CROP
            dialog.dismiss() }
        dialog.findViewById<RelativeLayout>(R.id.none).setOnClickListener { binding.gridBackground.setImageDrawable(null)
            binding.gridBackground.scaleType=ImageView.ScaleType.FIT_XY
            dialog.dismiss() }
        dialog.findViewById<Button>(R.id.cancel).setOnClickListener{dialog.dismiss()}
        dialog.show()
    }


}