package com.example.todo.appwidget
import android.content.Intent
import android.widget.RemoteViewsService

//returns instance of adapter for remote views
class MyWidgetRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return MyWidgetRemoteViewsFactory(this.applicationContext, intent)
    }
}