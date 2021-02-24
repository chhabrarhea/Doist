package com.example.todo.fragments.draw

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context,attr:AttributeSet): View(context,attr) {
    private var drawPath:CustomDrawPath?=null
    private var canvasBitmap:Bitmap?=null
    private var drawPaint: Paint?=null
    private var canvasPaint:Paint?=null
    private var brushSize=10.toFloat()
    private var color=Color.BLACK
    private var canvas:Canvas?=null
    private val paths=ArrayList<CustomDrawPath>()

    init {
        drawPaint= Paint()
        drawPath=CustomDrawPath(color,brushSize)
        drawPaint!!.color=color
        drawPaint!!.strokeJoin=Paint.Join.ROUND
        drawPaint!!.style=Paint.Style.STROKE
        drawPaint!!.strokeCap=Paint.Cap.ROUND
        canvasPaint= Paint(Paint.DITHER_FLAG)

    }
//    ARGB_8888- Each pixel is stored on 4 bytes.
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap= Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas= Canvas(canvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(canvasBitmap!!,0f,0f,canvasPaint)
        for(path in paths){
            drawPaint!!.color=path.color
            drawPaint!!.strokeWidth=path.brushThickness
            canvas.drawPath(path,drawPaint!!)
        }
        if (!drawPath!!.isEmpty){
            drawPaint!!.color=drawPath!!.color
            drawPaint!!.strokeWidth=drawPath!!.brushThickness
        canvas.drawPath(drawPath!!,drawPaint!!)}
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX=event?.x
        val touchY=event?.y
        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                drawPath!!.color=color
                drawPath!!.brushThickness=brushSize
// Clear any lines and curves from the path, making it empty. This does NOT change the fill-type setting.
                drawPath!!.reset()
                drawPath!!.moveTo(touchX!!,touchY!!)
            }
            MotionEvent.ACTION_MOVE->{
                drawPath!!.lineTo(touchX!!,touchY!!)
            }
            MotionEvent.ACTION_UP->{
                paths.add(drawPath!!)
                drawPath=CustomDrawPath(color,brushSize)
            }
            else->return false
        }
//when you are dealing with drawing, you have to tell the system that its underlying data for resizing, hiding, showing etc.
// is not in a good state with Widget.invalidate() and the re-drawing gets queued on the main thread
//        invalidate must be called on the UI thread only.
        invalidate()
        return true
    }

    fun setSizeForBrush(newSize:Float){
        brushSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize,resources.displayMetrics)
        drawPaint!!.strokeWidth=brushSize
    }

    fun setBrushColor(c:Int){
        color=c
        drawPaint!!.color=color
    }

    internal inner class CustomDrawPath(var color:Int,var brushThickness:Float):Path()
}