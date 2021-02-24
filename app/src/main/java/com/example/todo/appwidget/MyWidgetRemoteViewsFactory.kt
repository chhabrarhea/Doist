package com.example.todo.appwidget

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.util.Log
import android.widget.AdapterView
import android.widget.RemoteViews
import android.widget.RemoteViewsService.RemoteViewsFactory
import com.example.todo.NewAppWidget
import com.example.todo.R
import com.example.todo.data.ToDoDao
import com.example.todo.data.ToDoDatabase
import com.example.todo.data.models.ToDoData


//adapter for remote views
class MyWidgetRemoteViewsFactory(private val mContext: Context, intent: Intent?) :
    RemoteViewsFactory {

    lateinit var list:List<ToDoData>
    var dao:ToDoDao = ToDoDatabase.getDatabase(mContext).toDoDao()
    override fun onCreate() {

    }

    override fun onDataSetChanged() {
        list=dao.getData()
    }

    override fun onDestroy() {

    }

    override fun getCount(): Int {
      return list.size
    }

    override fun getViewAt(p0: Int): RemoteViews? {
        if (p0==AdapterView.INVALID_POSITION  || p0>list.size)
            return null
        val rv=RemoteViews(mContext.packageName, R.layout.collection_widget_list_item)
        rv.setTextViewText(R.id.widget_title, list[p0].title)
        rv.setTextViewText(R.id.widget_desc, list[p0].description)
        rv.removeAllViews(R.id.container)
        if (list[p0].checklist!=null && list[p0].checklist!!.size>0){
            for(checklist in list[p0].checklist!!){
                Log.i("hhj", checklist.task)
                val view=RemoteViews(mContext.packageName, R.layout.widget_checklist)
                view.setTextViewText(R.id.list_item, checklist.task)
                if(checklist.done){
                    view.setImageViewResource(R.id.checkbox, R.drawable.ic_check_circle_filled)
                view.setInt(
                    R.id.list_item,
                    "setPaintFlags",
                    Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
                )}
                rv.addView(R.id.container, view)
            }
        }

        val fillInIntent = Intent()
        fillInIntent.putExtra(NewAppWidget().EXTRA_LABEL, list[p0])
        rv.setOnClickFillInIntent(R.id.widgetItemContainer, fillInIntent)

        return rv
    }

    override fun getLoadingView(): RemoteViews?{
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(p0: Int): Long {
        return list[p0].id.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }


}