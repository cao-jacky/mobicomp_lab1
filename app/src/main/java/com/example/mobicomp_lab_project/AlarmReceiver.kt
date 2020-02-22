package com.example.mobicomp_lab_project

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.util.*
import kotlin.random.Random

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderItem= intent.getStringExtra("Reminder")
        Log.d("debugging", reminderItem)
        Toast.makeText(context, reminderItem, Toast.LENGTH_LONG).show()

        val CHANNEL_ID = "REMINDER_NOTIFICATION_CHANNEL"
        var notificationId = 1589
        notificationId += Random(notificationId).nextInt(1, 30)
        val message: String = "Notification from MobiComp App!"
        var notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            //.setSmallIcon(R.drawable.ic_alarm_24px)
            .setContentTitle(context?.getString(R.string.app_name))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { val channel = NotificationChannel(
            CHANNEL_ID, context?.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context?.getString(R.string.app_name)
        }
            notificationManager.createNotificationChannel(channel) }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}