package com.spisoft.quicknote.databases

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.SystemClock
import android.preference.PreferenceManager
import com.spisoft.quicknote.Note
import com.spisoft.quicknote.R
import com.spisoft.quicknote.reminders.NotificationPublisher
import com.spisoft.sync.Log
import com.spisoft.sync.database.SyncDatabase
import java.util.*
import kotlin.collections.ArrayList


class RemindersManager(ct: Context){
    private var alarmMgr: AlarmManager
    private val ct: Context = ct
    private val TAG = "RemindersManager"
    init {
        alarmMgr = ct.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    }



    private fun setAlarm(note:Note, time:Long):Int{
        var requestCode = PreferenceManager.getDefaultSharedPreferences(ct).getInt("last_reminder_request_code",2600) + 1
        //select a request code not in DB
        val database = Database.getInstance(ct)!!
        var requestCodes = ArrayList<Int>()
        synchronized(database.lock) {
            val sqLiteDatabase = database.open()
            val cursor = sqLiteDatabase.query(TABLE_NAME, COLUMNS, null, null, null, null, null)
            if(cursor.count > 0){
                val requestCodeCol = cursor.getColumnIndex(KEY_REQUEST_CODE)
                while(cursor.moveToNext()){
                    requestCodes.add(cursor.getInt(requestCodeCol))
                }
                cursor.close()
            }
            database.close()
        }
        while(requestCodes.contains(requestCode))
            requestCode ++ ;
        val alarmIntent = Intent(ct, NotificationPublisher::class.java).let { intent ->
            intent.putExtra(NotificationPublisher.NOTIFICATION_ID, 123);
            intent.putExtra(NotificationPublisher.NOTE_PATH, note.path);
            intent.putExtra(NotificationPublisher.NOTIFICATION, getNotification(note));
            PendingIntent.getBroadcast(ct, requestCode, intent, 0)
        }
        alarmMgr?.set(
                AlarmManager.RTC_WAKEUP,
                time,
                alarmIntent
        )
        PreferenceManager.getDefaultSharedPreferences(ct).edit().putInt("last_reminder_request_code",requestCode).apply()
        return requestCode
    }

    private fun cancelAlarm(requestCode:Int){
        val alarmIntent = Intent(ct, NotificationPublisher::class.java).let { intent ->

            PendingIntent.getBroadcast(ct, requestCode, intent, 0)
        }
        alarmMgr?.cancel(alarmIntent)
    }
    private fun getNotification(note: Note): Notification {
        val builder = Notification.Builder(ct)
        builder.setContentTitle(ct.getString(R.string.reminder))
        if(!note.title!!.startsWith("untitled") || note.shortText == null || note.shortText.length == 0)
            builder.setContentText(note.title)
        else
            builder.setContentText(if(note.shortText.length>15) note.shortText.substring(0,15) else note.shortText)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        return builder.build()
    }
    fun add(note: Note){
        remove(note.path)
        var next:Long = -1;
        Log.d(TAG, "Note with "+note.mMetadata.reminders.size+" reminders")
        var selRem: Note.Reminder? = null
        for(reminder in note.mMetadata.reminders){
            if(reminder.frequency.equals("once")){
                val timeCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                timeCal.timeInMillis = reminder.time
                val date = Calendar.getInstance()
                date.timeInMillis = 0
                date.set(Calendar.YEAR, reminder.year)
                date.set(Calendar.MONTH, reminder.month-1)
                date.set(Calendar.DAY_OF_MONTH, reminder.dayOfMonth)
                date.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                date.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                Log.d(TAG, "once with "+date.timeInMillis)
                Log.d(TAG, "once day "+reminder.dayOfMonth)
                Log.d(TAG, "once month "+reminder.month)
                Log.d(TAG, "once year "+reminder.year)
                if(System.currentTimeMillis() < date.timeInMillis && (next == -1.toLong() || next > date.timeInMillis)){
                    next = date.timeInMillis
                    selRem = reminder
                }
            }
            else
            if(reminder.frequency.equals("days")){
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
                    date1.timeInMillis = System.currentTimeMillis()
                    date1.set(Calendar.HOUR_OF_DAY, 0)
                    date1.set(Calendar.MINUTE, 0)
                    date1.set(Calendar.SECOND, 0)
                    date1.set(Calendar.MILLISECOND, 0)
                    date1.timeInMillis += reminder.time

                    while (date1.get(Calendar.DAY_OF_WEEK) !== dayInt || date1.timeInMillis <= System.currentTimeMillis()) {
                        date1.add(Calendar.DATE, 1)
                    }
                    if(date1.timeInMillis < next || next < 0)
                        next = date1.timeInMillis
                    selRem = reminder
                }

            }

        }
        Log.d(TAG, "next in "+ (next - System.currentTimeMillis()))
        Log.d(TAG, "time "+ selRem?.time)

        val code = setAlarm(note, next)
        val database = Database.getInstance(ct)!!
        synchronized(database.lock) {
            val sqLiteDatabase = database.open()
            val initialValues = ContentValues()
            initialValues.put(KEY_PATH, note.path)
            initialValues.put(KEY_TIME, next)
            initialValues.put(KEY_REQUEST_CODE, code)
            sqLiteDatabase.insertWithOnConflict(TABLE_NAME, null, initialValues, SQLiteDatabase.CONFLICT_IGNORE)
            database.close()
        }
    }
    fun remove(path: String){
        val database = Database.getInstance(ct)!!
        synchronized(database.lock) {
            val sqLiteDatabase = database.open()
            val cursor = sqLiteDatabase.query(TABLE_NAME, COLUMNS, KEY_PATH+" = ?", arrayOf(path), null, null, null)
            if(cursor.count > 0){
                val pathCol = cursor.getColumnIndex(KEY_PATH)
                val requestCodeCol = cursor.getColumnIndex(KEY_REQUEST_CODE)
                val timeCol = cursor.getColumnIndex(KEY_TIME)
                while(cursor.moveToNext()){
                    cancelAlarm(cursor.getInt(requestCodeCol))
                }
                cursor.close()
            }
            sqLiteDatabase.delete(TABLE_NAME, KEY_PATH+" = ?", arrayOf(path))
            database.close()
        }


    }

    fun onBoot(){
        PreferenceManager.getDefaultSharedPreferences(ct).edit().putInt("last_reminder_request_code",2600).apply()
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
        var COLUMNS = arrayOf(KEY_PATH, KEY_REQUEST_CODE, KEY_TIME)
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