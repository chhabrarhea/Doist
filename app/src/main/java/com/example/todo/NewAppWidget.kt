package com.example.todo

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.todo.appwidget.MyWidgetRemoteViewsService


/**
 * Implementation of App Widget functionality.
 */
class NewAppWidget : AppWidgetProvider() {
    val EXTRA_LABEL = "currentItem"
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(
                context.packageName,
                R.layout.new_app_widget
            )
            //set adapter for remote views
            val intent = Intent(context, MyWidgetRemoteViewsService::class.java)
            views.setRemoteAdapter(R.id.widgetListView, intent)
            appWidgetManager.updateAppWidget(appWidgetId, views)

            //onClickListener for title, launches app
            val titleIntent = Intent(context, MainActivity::class.java)
            val titlePendingIntent = PendingIntent.getActivity(context, 0, titleIntent, 0)
            views.setOnClickPendingIntent(R.id.widgetTitleLabel, titlePendingIntent)


//             template to handle the click listener for each item
            val clickIntentTemplate = Intent(context,MainActivity::class.java)
            val clickPendingIntentTemplate: PendingIntent = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(clickIntentTemplate)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setPendingIntentTemplate(R.id.widgetListView, clickPendingIntentTemplate)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
    fun sendRefreshBroadcast(context: Context) {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        intent.component = ComponentName(context, NewAppWidget::class.java)
        context.sendBroadcast(intent)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            // refresh all your widgets
            val mgr = AppWidgetManager.getInstance(context)
            val cn = ComponentName(context, NewAppWidget::class.java)
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.widgetListView)
        }
        super.onReceive(context, intent)
    }
}
