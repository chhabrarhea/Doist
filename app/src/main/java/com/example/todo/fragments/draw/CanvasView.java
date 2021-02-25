package com.example.todo.fragments.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class CanvasView extends View {

    public int width;

    public int height;


    private Bitmap mBitmap;
    private Canvas mCanvas;

    private CustomPath mPath;

    Context context;

    private Paint mPaint;

    private float mX, mY;

    private static final float TOLERANCE = 5;
    private final ArrayList<CustomPath> paths = new ArrayList<>();
    private final ArrayList<CustomPath> undonePaths = new ArrayList<>();

    private final Paint canvasPaint;

    private boolean isErasemode = false;


    public CanvasView(Context c, AttributeSet attrs) {

        super(c, attrs);

        context = c;

        canvasPaint= new Paint(Paint.DITHER_FLAG);

        mPaint = new Paint();


        mPaint.setColor(Color.YELLOW);

        mPaint.setStyle(Paint.Style.STROKE);

        mPaint.setStrokeJoin(Paint.Join.ROUND);

        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mPaint.setStrokeWidth(2f);

        mPath = new CustomPath(mPaint.getColor(),mPaint.getStrokeWidth());

        //float mEraserWidth = getResources().getDimension(R.dimen.eraser_size);


    }




    // override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);


        // your Canvas will draw onto the defined Bitmap

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        mCanvas = new Canvas(mBitmap);

    }


    // override onDraw

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0f, 0f, canvasPaint);
        super.onDraw(canvas);
        Log.i("nsd","onDraw");
        for(CustomPath p : paths) {
            Log.i("onDraw",p.toString());
            Paint paint=new Paint();
            paint.setColor(p.brushColor);
            paint.setStrokeWidth(p.brushSize);
            canvas.drawPath(p, paint);
        }
        if (!mPath.isEmpty()){
            canvas.drawPath(mPath, mPaint);}

    }

//    private void startTouch(float x, float y) {
//
//        undonePaths.clear();
//        mPath.reset();
//        mPath.moveTo(x, y);
//
//        mX = x;
//
//        mY = y;
//    }

    public void onClickUndo() {
        if (paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
            invalidate();
        } else {
            //Util.Imageview_undo_redum_Status=false;
        }
        //toast the user
    }

    public void onEraser(){
        if(!isErasemode){
            isErasemode = true;
        }else{
            isErasemode = false;
        }
    }

    private void remove(int index){
        paths.remove(index);
        invalidate();
    }

    public void onClickRedo() {
        if (undonePaths.size() > 0) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            invalidate();
        } else {
            // Util.Imageview_undo_redum_Status=false;
        }
        //toast the user
    }

    // when ACTION_MOVE move touch according to the x,y values

//    private void moveTouch(float x, float y) {
//
//        float dx = Math.abs(x - mX);
//
//        float dy = Math.abs(y - mY);
//
//        if (dx >= TOLERANCE || dy >= TOLERANCE) {
//
//            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
//
//            mX = x;
//
//            mY = y;
//
//        }
//
//    }
//
//
//    private void upTouch() {
//
//
//        mPath.lineTo(mX, mY);
//
//        mPath.lineTo(mX, mY);
//        // commit the path to our offscreen
//        mCanvas.drawPath(mPath, mPaint);
//        // kill this so we don't double draw
//        paths.add(mPath);
//        mPath = new CustomPath(mPaint.getColor(),mPaint.getStrokeWidth());
//    }


    //override the onTouchEvent

    @Override

    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();

        float y = event.getY();


        if(isErasemode){
            for(int i = 0;i<paths.size();i++){
                RectF r = new RectF();
                Point pComp = new Point((int) (event.getX()), (int) (event.getY() ));
                Path mPath = paths.get(i);
                mPath.computeBounds(r, true);
                if (r.contains(pComp.x, pComp.y)) {
                    remove(i);
                    break;
                }
            }
            return false;
        }
        else {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                        mPath.reset();
                        mPath.moveTo(x,y);
                    invalidate();
                    break;

                case MotionEvent.ACTION_MOVE:
                       mPath.lineTo(x,y);
                       invalidate();
                       break;

                case MotionEvent.ACTION_UP:

                    paths.add(mPath);
//                    mPath=new CustomPath(mPaint.getColor(),mPaint.getStrokeWidth());

                    invalidate();

                    break;
                default:
                    return false;
            }

            return true;
        }

    }

    public void setSizeForBrush(Float size){
        float  brushSize=TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                size,
                getResources().getDisplayMetrics()
        );
        mPaint.setStrokeWidth(brushSize);
        mPath.setBrushSize(brushSize);
    }
    public void setBrushColor(int color){
        mPaint.setColor(color);
        mPath.setBrushColor(color);

    }
    class CustomPath extends Path{
        int brushColor;
        float brushSize;
        CustomPath(int c,float s){
            brushColor=c;
            brushSize=s;
        }

        public void setBrushColor(int brushColor) {
            this.brushColor = brushColor;
        }

        public void setBrushSize(float brushSize) {
            this.brushSize = brushSize;
        }
    }
}

