package com.example.todo.fragments.draw

import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.todo.R

class BrushSizePicker(val view:LinearLayout,val context: Context,val listener:SizeSelected) {
    private var prevStroke: RelativeLayout?=null
    private val brush5=view.findViewById(R.id.ib_5_brush) as RelativeLayout
    private val brush10=view.findViewById(R.id.ib_10_brush) as RelativeLayout
    private val brush15=view.findViewById(R.id.ib_15_brush) as RelativeLayout
    private val brush20=view.findViewById(R.id.ib_20_brush) as RelativeLayout
    private val brush25=view.findViewById(R.id.ib_25_brush) as RelativeLayout
    private val brush17=view.findViewById(R.id.ib_17_brush) as RelativeLayout
    
    init {
        brush10.setOnClickListener { updatePrevStroke(brush10) }
        brush5.setOnClickListener { 
            updatePrevStroke(brush5)}
       brush15.setOnClickListener { listener.sizeSelected(14.toFloat())
            updatePrevStroke(brush15)}
       brush17.setOnClickListener { listener.sizeSelected(16.5.toFloat())
            updatePrevStroke(brush17)}
       brush20.setOnClickListener { listener.sizeSelected(20.toFloat())
            updatePrevStroke(brush20)}
        brush25.setOnClickListener { listener.sizeSelected(25.toFloat())
            updatePrevStroke(brush25)}
    }

    private fun updatePrevStroke(relativeLayout: RelativeLayout) {
            if (prevStroke!=null){
                val card=prevStroke!!.getChildAt(0) as CardView
                card.setCardBackgroundColor(ContextCompat.getColor(context,R.color.darkGray))}
            val card=relativeLayout.getChildAt(0) as CardView
            card.setCardBackgroundColor(Color.BLACK)
            prevStroke=relativeLayout
        }
    
    interface SizeSelected{
        fun sizeSelected(size:Float)
    }
    }
