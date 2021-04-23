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
package com.spisoft.quicknote.widget

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.text.Html
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.spisoft.quicknote.MainActivity
import com.spisoft.quicknote.Note
import com.spisoft.quicknote.R
import com.spisoft.quicknote.databases.NoteManager
import com.spisoft.quicknote.notes_lister.LatestNotesLister
import com.spisoft.quicknote.notes_lister.PathNotesLister
import java.text.SimpleDateFormat
import java.util.*

class ListRemoteViewsFactory(app: Application, intent: Intent) : RemoteViewsService.RemoteViewsFactory {
    private val WIDTH = 80
    private val HEIGHT = 80
    private val app = app
    private val appWidgetId: Int
    private var notes: List<Any>? = null
    private val path:String ?= null

    override fun onCreate() {
        if(path != null)
            notes = PathNotesLister(path, app).getNotes()
        else
            notes = LatestNotesLister(app).getNotes()
    }

    override fun onDataSetChanged() {
        if(path != null)
            notes = PathNotesLister(path, app).getNotes()
        else
            notes = LatestNotesLister(app).getNotes()
    }

    override fun onDestroy() {
    }

    override fun getCount(): Int {
        return notes!!.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val note: Note = notes!![position] as Note
        val noteText = note.shortText
        val noteCreationDate: Long = note.mMetadata.creation_date
        val noteLastModificationDate: Long = note.mMetadata.last_modification_date

        var date = "";

        if (noteCreationDate.compareTo(-1) != 0) {
            date = SimpleDateFormat("dd/MM/yyyy").format(Date(noteCreationDate))
        }

        if(noteLastModificationDate.compareTo(-1) != 0) {
            date = SimpleDateFormat("dd/MM/yyyy").format(Date(noteLastModificationDate))
        }

        val intentDetail = Intent(app.applicationContext, MainActivity::class.java)
        intentDetail.action = Companion.ACTION_OPEN_NOTE
        intentDetail.putExtra("note_path", note.path)
        intentDetail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intentDetail.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intentDetail.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val row = RemoteViews(app.packageName, R.layout.note_layout_widget)

        row.setOnClickFillInIntent(R.id.root, intentDetail)
        var title = if(note.title.startsWith("untitled")) "" else note.title
        row.setTextViewText(R.id.note_content, noteText)
        row.setTextViewText(R.id.note_title, title)
        if(title.isEmpty())
            row.setViewVisibility(R.id.note_title, View.GONE)
        else{
            row.setViewVisibility(R.id.note_title, View.VISIBLE)
        }
        row.setTextViewText(R.id.note_date, date)
        var todo = "";
        note.mMetadata.todolists.map {
            it.todo.map {
                todo += "☐ "+it+"\n"
            }
        }

        if(todo.isEmpty())
            row.setViewVisibility(R.id.note_todo, View.GONE)
        else{
            row.setTextViewText(R.id.note_todo, todo)
            row.setViewVisibility(R.id.note_todo, View.VISIBLE)
        }
        return row
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    init {
        appWidgetId = intent
                .getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    }

    companion object {
        private const val ACTION_OPEN_NOTE = "open_note"
    }
}