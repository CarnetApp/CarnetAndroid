package com.spisoft.quicknote.reminders


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.spisoft.sync.Log

class NotificationPublisher : BroadcastReceiver() {
    val TAG = "NotificationPublisher"
    override fun onReceive(context: Context, intent: Intent) {

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = intent.getParcelableExtra<Notification>(NOTIFICATION)
        val path = intent.getStringExtra(NOTE_PATH)
        val id = intent.getIntExtra(NOTIFICATION_ID, 4)
        if(notification != null) {
            notificationManager.notify(id, notification)
            if(path != null)
                RemindersManager.getInstance(context)!!.onNotified(path)
        }

        Log.d(TAG, "notification "+(notification == null))
        Log.d(TAG, "notification id "+id)

    }

    companion object {

        var NOTIFICATION_ID = "notification-id"
        var NOTIFICATION = "notification"
        var NOTE_PATH = "note_path"
        val CHANNEL_ID: String = "channel_id"
    }
}