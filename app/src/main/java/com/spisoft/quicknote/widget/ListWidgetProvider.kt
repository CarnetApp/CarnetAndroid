package com.spisoft.quicknote.widget

/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.SparseArray
import android.widget.RemoteViews
import com.spisoft.quicknote.MainActivity
import com.spisoft.quicknote.R


class ListWidgetProvider : WidgetProvider() {
    override fun getRemoteViews(context: Context?, widgetId: Int, pendingIntentsMap: SparseArray<PendingIntent>): RemoteViews? {

        val views = RemoteViews(context?.packageName, R.layout.widget_layout_list)
        views.setOnClickPendingIntent(R.id.list, pendingIntentsMap.get(R.id.list))
        views.setOnClickPendingIntent(R.id.add, pendingIntentsMap.get(R.id.add))

        // Set up the intent that starts the ListViewService, which will
        // provide the views for this collection.
        val intent = Intent(context, ListWidgetService::class.java)
        // Add the app widget ID to the intent extras.
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

        views.setRemoteAdapter(R.id.widget_list, intent)

        val clickIntent = Intent(context, MainActivity::class.java)
        clickIntent.action = "action_widget"
        val clickPI = PendingIntent.getActivity(context, 0,
                clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        views.setPendingIntentTemplate(R.id.widget_list, clickPI)

        return views
    }
}