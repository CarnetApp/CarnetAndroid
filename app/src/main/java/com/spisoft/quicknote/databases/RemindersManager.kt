package com.spisoft.quicknote.databases

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.spisoft.quicknote.Note
import com.spisoft.quicknote.R
import com.spisoft.quicknote.reminders.NotificationPublisher
import java.util.*

class RemindersManager(ct: Context){
    private var alarmMgr: AlarmManager
    private val ct: Context = ct

    init {
        alarmMgr = ct.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    }



    private fun setAlarm(note:Note, time:Long):Int{
        val requestCode = 1000
        val alarmIntent = Intent(ct, NotificationPublisher::class.java).let { intent ->
            intent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
            intent.putExtra(NotificationPublisher.NOTIFICATION, getNotification(note.title));
            PendingIntent.getBroadcast(ct, requestCode, intent, 0)
        }

        alarmMgr?.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                time,
                alarmIntent
        )
        return requestCode
    }

    private fun cancelAlarm(requestCode:Int){
        val alarmIntent = Intent(ct, NotificationPublisher::class.java).let { intent ->

            PendingIntent.getBroadcast(ct, requestCode, intent, 0)
        }
        alarmMgr?.cancel(alarmIntent)
    }
    private fun getNotification(content: String): Notification {
        val builder = Notification.Builder(ct)
        builder.setContentTitle("Reminder")
        builder.setContentText(content)
        builder.setSmallIcon(R.drawable.ic_launcher)
        return builder.build()
    }
    fun add(note: Note){

        for(reminder in note.mMetadata.reminders){
            var next:Long = -1;
            if(reminder.frequency.equals("once")){
                if(SystemClock.elapsedRealtime() < reminder.time + reminder.date){
                    next = reminder.date + reminder.time
                }
            }
            else
            if(reminder.frequency.equals("days")){
                var nextReminder:Long = -1;
                for(day in reminder.days){
                    val dayInt:Int = when(day) {
                        "monday" -> Calendar.MONDAY
                        "tuesday" -> Calendar.TUESDAY
                        "wednesday" -> Calendar.WEDNESDAY
                        "thursday" -> Calendar.THURSDAY
                        "friday" -> Calendar.FRIDAY
                        "saturday" -> Calendar.SATURDAY
                        "sunday" -> Calendar.SUNDAY
                        else -> return
                    }

                    val date1 = Calendar.getInstance()
                    date1.timeInMillis = SystemClock.elapsedRealtime()
                    date1.set(Calendar.HOUR_OF_DAY, 0)
                    date1.set(Calendar.MINUTE, 0)
                    date1.set(Calendar.SECOND, 0)
                    date1.set(Calendar.MILLISECOND, 0)
                    date1.timeInMillis += reminder.time

                    while (date1.get(Calendar.DAY_OF_WEEK) !== dayInt || date1.timeInMillis <= SystemClock.elapsedRealtime()) {
                        date1.add(Calendar.DATE, 1)
                    }
                    if(date1.timeInMillis < nextReminder || nextReminder < 0)
                        nextReminder = date1.timeInMillis
                }

                setAlarm(note, nextReminder)
            }
        }
    }
    fun remove(path: String){

    }

    fun refresh(){
        CacheManager.getInstance(ct).cache.values.map { note -> add(note) }
    }

    fun onNotified(path:String){

        add(CacheManager.getInstance(ct).get(path))

    }

    companion object {
        val TABLE_NAME = "reminders"
        val KEY_PATH = "path"
        val KEY_REQUEST_CODE = "request_code"
        val KEY_TIME = "time"

        val CREATE_TABLE = ("create table " + TABLE_NAME + "( "
                + KEY_PATH + " TEXT,"
                + KEY_REQUEST_CODE + " integer, "
                + KEY_TIME + " integer);");
        var instance: RemindersManager? = null;
        fun getInstance(ct:Context):RemindersManager?{
            if(instance == null){
                instance = RemindersManager(ct)
            }
            return instance
        }
    }

}