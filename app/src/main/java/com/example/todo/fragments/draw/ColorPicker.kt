package com.example.todo.fragments.draw

import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import com.example.todo.R

class ColorPicker(val view: LinearLayout, val listener: ColorPicked) {
    private var prevColor: CardView? = null
    private val black = view.findViewById(R.id.black) as CardView
    private val red = view.findViewById(R.id.red) as CardView
    private val purple = view.findViewById(R.id.purple) as CardView
    private val pink = view.findViewById(R.id.pink) as CardView
    private val blue = view.findViewById(R.id.blue) as CardView
    private val green = view.findViewById(R.id.green) as CardView
    private val brown = view.findViewById(R.id.brown) as CardView
    private val cyan = view.findViewById(R.id.cyan) as CardView

    init {
        prevColor = view.findViewById(R.id.black)
        black.setOnClickListener { updatePrevColor(black) }
        red.setOnClickListener { updatePrevColor(red) }
        pink.setOnClickListener { updatePrevColor(pink) }
        purple.setOnClickListener { updatePrevColor(purple) }
        green.setOnClickListener { updatePrevColor(green) }
        blue.setOnClickListener { updatePrevColor(blue) }
        brown.setOnClickListener { updatePrevColor(brown) }
        cyan.setOnClickListener { updatePrevColor(cyan) }
    }

    private fun updatePrevColor(card: CardView) {
        if (prevColor != null) {
            val button = prevColor!!.getChildAt(0) as ImageButton
            button.visibility = View.GONE
        }
        listener.colorPicked(card.cardBackgroundColor.defaultColor)
        val button = card.getChildAt(0) as ImageButton
        button.visibility = View.VISIBLE
        prevColor = card
    }

    interface ColorPicked {
        fun colorPicked(color: Int)
    }


}