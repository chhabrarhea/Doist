package com.example.todo.fragments.draw

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import android.widget.ImageButton
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
private var prevStroke:RelativeLayout?=null
    private var prevColor:CardView?=null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding= FragmentDrawBinding.inflate(layoutInflater,container,false)
        sheetBehavior=BottomSheetBehavior.from(binding.bottomSheet)
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
        initStrokeOptions()
        initColorOptions()
        binding.detail.eraser.setOnClickListener{
            binding.canvas.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            binding.canvas.setErase(true)
            binding.detail.pen.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
            binding.detail.eraser.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.secondaryColor))
        }
        binding.detail.pen.setOnClickListener{
            binding.canvas.setErase(false)
            binding.canvas.setBrushColor(prevColor!!.cardBackgroundColor.defaultColor)
            binding.detail.pen.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.secondaryColor))
            binding.detail.eraser.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        }
        return binding.root
    }

    private fun initStrokeOptions() {
            binding.detail.ib10Brush.setOnClickListener {
                binding.canvas.setSizeForBrush(9.toFloat())
            updatePrevStroke(binding.detail.ib10Brush)}
            binding.detail.ib5Brush.setOnClickListener { binding.canvas.setSizeForBrush(5.toFloat())
            updatePrevStroke( binding.detail.ib5Brush)}
            binding.detail.ib15Brush.setOnClickListener { binding.canvas.setSizeForBrush(14.toFloat())
            updatePrevStroke(binding.detail.ib15Brush)}
            binding.detail.ib17Brush.setOnClickListener { binding.canvas.setSizeForBrush(16.5.toFloat())
            updatePrevStroke(binding.detail.ib17Brush)}
            binding.detail.ib20Brush.setOnClickListener { binding.canvas.setSizeForBrush(20.toFloat())
            updatePrevStroke(binding.detail.ib20Brush)}
            binding.detail.ib25Brush.setOnClickListener { binding.canvas.setSizeForBrush(25.toFloat())
            updatePrevStroke(binding.detail.ib25Brush)}

    }

    private fun initColorOptions(){
        prevColor=binding.detail.black
        binding.detail.black.setOnClickListener{ updatePrevColor( binding.detail.black)}
        binding.detail.red.setOnClickListener{updatePrevColor( binding.detail.red)}
        binding.detail.pink.setOnClickListener{updatePrevColor( binding.detail.pink)}
        binding.detail.purple.setOnClickListener{updatePrevColor( binding.detail.purple)}
        binding.detail.green.setOnClickListener{updatePrevColor( binding.detail.green)}
        binding.detail.blue.setOnClickListener{updatePrevColor(binding.detail.blue)}
        binding.detail.brown.setOnClickListener{updatePrevColor(binding.detail.brown)}
        binding.detail.cyan.setOnClickListener{updatePrevColor(binding.detail.cyan)}
    }

    private fun updatePrevStroke(relativeLayout: RelativeLayout) {
          if (prevStroke!=null){
              val card=prevStroke!!.getChildAt(0) as CardView
              card.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.darkGray))}
        val card=relativeLayout.getChildAt(0) as CardView
        card.setCardBackgroundColor(Color.BLACK)
        prevStroke=relativeLayout
    }

    private fun updatePrevColor(card:CardView){
        if (prevColor!=null){
            val button=prevColor!!.getChildAt(0) as ImageButton
            button.visibility=View.GONE
        }
        binding.canvas.setBrushColor(card.cardBackgroundColor.defaultColor)

        val button=card.getChildAt(0) as ImageButton
        button.visibility=View.VISIBLE
        prevColor=card
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
        dialog.setContentView(R.layout.grid_dialog);
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