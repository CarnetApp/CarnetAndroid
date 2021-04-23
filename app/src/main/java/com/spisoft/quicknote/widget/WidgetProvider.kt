package com.spisoft.quicknote.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.SparseArray
import android.widget.RemoteViews
import com.spisoft.quicknote.MainActivity
import com.spisoft.quicknote.MainActivity.ACTION_WIDGET
import com.spisoft.quicknote.R
import com.spisoft.sync.utils.Utils


/**
 * Implementation of App Widget functionality.
 */
abstract class WidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            setLayout(context, appWidgetManager, appWidgetId)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private fun setLayout(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        // Create an Intent to launch MainActivity
        val intentList = Intent(context, MainActivity::class.java)
        val pendingIntentList: PendingIntent = PendingIntent
                .getActivity(Utils.context, appWidgetId, intentList, FLAG_ACTIVITY_NEW_TASK)

        // Create an Intent to launch BlankFragment
        val intentAdd = Intent(Utils.context, MainActivity::class.java)
        intentAdd.action = ACTION_WIDGET
        intentAdd.putExtra("widget_id", appWidgetId)
        intentAdd.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intentAdd.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intentAdd.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val pendingIntentAdd = PendingIntent
                .getActivity(Utils.context, appWidgetId, intentAdd, FLAG_ACTIVITY_NEW_TASK)

        // Creation of a map to associate PendingIntent(s) to views
        val map: SparseArray<PendingIntent> = SparseArray()
        map.put(R.id.list, pendingIntentList)
        map.put(R.id.add, pendingIntentAdd)

        val views = getRemoteViews(context, appWidgetId, map);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    abstract fun getRemoteViews(context: Context?, widgetId: Int, pendingIntentsMap: SparseArray<PendingIntent>): RemoteViews?
}

