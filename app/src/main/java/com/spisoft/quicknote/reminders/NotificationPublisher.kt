package com.spisoft.quicknote.reminders


import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.spisoft.sync.Log

class NotificationPublisher : BroadcastReceiver() {
    val TAG = "NotificationPublisher"
    override fun onReceive(context: Context, intent: Intent) {

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = intent.getParcelableExtra<Notification>(NOTIFICATION)
        val path = intent.getStringExtra(NOTE_PATH)
        val id = intent.getIntExtra(NOTIFICATION_ID, 4)
        if(notification != null)
            notificationManager.notify(id, notification)
        else
            RemindersManager.getInstance(context)!!.onNotified(path)

        Log.d(TAG, "notification "+(notification == null))
        Log.d(TAG, "notification id "+id)

    }

    companion object {

        var NOTIFICATION_ID = "notification-id"
        var NOTIFICATION = "notification"
        var NOTE_PATH = "note_path"
    }
}