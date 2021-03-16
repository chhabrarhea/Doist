package com.example.todo.fragments.draw

import android.content.Context

import android.widget.LinearLayout

import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.todo.R

class BrushSizePicker(val view:LinearLayout,val context: Context,val listener:SizeSelected) {
    private var prevStroke: CardView?=null
    private val brush5=view.findViewById(R.id.ib_5_brush)   as CardView
    private val brush10=view.findViewById(R.id.ib_10_brush) as CardView
    private val brush15=view.findViewById(R.id.ib_15_brush) as CardView
    private val brush20=view.findViewById(R.id.ib_20_brush) as CardView
    private val brush25=view.findViewById(R.id.ib_25_brush) as CardView
    private val brush17=view.findViewById(R.id.ib_17_brush) as CardView
    
    init {
        listener.sizeSelected(10.toFloat())
        updatePrevStroke(brush5)
        brush10.setOnClickListener { listener.sizeSelected(10.toFloat())
            updatePrevStroke(brush10) }
        brush5.setOnClickListener {  listener.sizeSelected(4.toFloat())
            updatePrevStroke(brush5)}
       brush15.setOnClickListener { listener.sizeSelected(14.toFloat())
            updatePrevStroke(brush15)}
       brush17.setOnClickListener { listener.sizeSelected(17.5.toFloat())
            updatePrevStroke(brush17)}
       brush20.setOnClickListener { listener.sizeSelected(22.toFloat())
            updatePrevStroke(brush20)}
        brush25.setOnClickListener { listener.sizeSelected(27.toFloat())
            updatePrevStroke(brush25)}
    }

    private fun updatePrevStroke(cardView: CardView) {
            if (prevStroke!=null){
                prevStroke!!.setCardBackgroundColor(ContextCompat.getColor(context,R.color.white)) }
            cardView.setCardBackgroundColor(ContextCompat.getColor(context,R.color.secondaryColor))
            prevStroke=cardView
        }
    
    interface SizeSelected{
        fun sizeSelected(size:Float)
    }
    }
