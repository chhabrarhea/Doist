package com.example.todo

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.example.todo.data.Repository.ToDoRepository
import com.example.todo.data.ToDoDao
import com.example.todo.data.ToDoDatabase
import com.example.todo.data.TodoViewModel
import com.example.todo.data.models.ToDoData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NotifierAlarm :BroadcastReceiver() {
    lateinit var dao:ToDoDao
    override fun onReceive(p0: Context, p1: Intent) {

        val bundle=p1.getBundleExtra("currentItem")
        val todo=bundle?.getParcelable<ToDoData>("currentItem")

        val alarm=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val intent1 = Intent(p0, MainActivity::class.java)
        intent1.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent1.putExtra("currentItem", todo)
        val taskStackBuilder = TaskStackBuilder.create(p0)
        taskStackBuilder.addParentStack(MainActivity::class.java)
        taskStackBuilder.addNextIntent(intent1)
        val intent2 = taskStackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT)


        var channel:NotificationChannel?=null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel(
                "my_channel_01",
                "doist",
                NotificationManager.IMPORTANCE_HIGH
            )
        }
        val builder = Notification.Builder(p0,channel!!.id)
        var desc=todo?.description
        if(desc.equals("") && todo?.checklist!=null){
            for (check in todo.checklist!!)
                desc+=check.task+"\n"
        }
       val notification = builder.setContentTitle(todo?.title)
            .setContentText(desc).setAutoCancel(true)
            .setSound(alarm).setSmallIcon(R.drawable.ic_note)
            .setContentIntent(intent2)
            .setChannelId("my_channel_01")
            .build()

        val  notificationManager =
            p0.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(todo!!.id, notification)
        dao=ToDoDatabase.getDatabase(p0.applicationContext).toDoDao()
        CoroutineScope(Dispatchers.IO).launch {
            dao.deleteReminder(todo.id)
        }


    }
}