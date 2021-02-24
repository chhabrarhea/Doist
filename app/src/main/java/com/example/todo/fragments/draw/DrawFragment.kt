package com.example.todo.fragments.draw

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentDrawBinding.inflate(layoutInflater,container,false)
        sheetBehavior=BottomSheetBehavior.from(binding.bottomSheet)
        prevColor=binding.detail.black
        initStrokeOptions()
        initColorOptions()
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


}