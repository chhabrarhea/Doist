package com.example.todo.fragments.list

import android.R
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


abstract class SwipeToDelete(private val mContext: Context) : ItemTouchHelper.Callback() {

    private var mClearPaint: Paint
    private var mBackground: ColorDrawable = ColorDrawable()
    private var backgroundColor = 0
    private var deleteDrawable: Drawable
    private var intrinsicWidth = 0
    private var intrinsicHeight = 0

    init {
        backgroundColor = Color.parseColor("#000000")
        mClearPaint = Paint()
        mClearPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
        deleteDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_delete)!!
        deleteDrawable.setTint(Color.parseColor("#000000"))
        intrinsicWidth = deleteDrawable.getIntrinsicWidth()
        intrinsicHeight = deleteDrawable.getIntrinsicHeight()
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(0, ItemTouchHelper.LEFT)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder1: RecyclerView.ViewHolder): Boolean {
        return false
    }

//    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
//        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//        val itemView: View = viewHolder.itemView
//        val itemHeight: Int = itemView.getHeight()
//        val isCancelled = dX == 0f && !isCurrentlyActive
//        if (isCancelled) {
//            clearCanvas(c, itemView.getRight() + dX, itemView.getTop().toFloat(), itemView.getRight().toFloat(), itemView.getBottom().toFloat())
//            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//            return
//        }
//        mBackground.setBounds(itemView.getRight() + dX.toInt(), itemView.getTop(), itemView.getRight(), itemView.getBottom())
//        mBackground.draw(c)
//        val deleteIconTop: Int = itemView.getTop() + (itemHeight - intrinsicHeight) / 2
//        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
//        val deleteIconLeft: Int = itemView.getRight() - deleteIconMargin - intrinsicWidth
//        val deleteIconRight: Int = itemView.getRight() - deleteIconMargin
//        val deleteIconBottom = deleteIconTop + intrinsicHeight
//        deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
//        deleteDrawable.draw(c)
//        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//    }

//    private fun clearCanvas(c: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
//        c.drawRect(left, top, right, bottom, mClearPaint)
//    }

    //    That means if the row is swiped less than 70%, the onSwipe method won’t be triggered.
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.7f
    }
}