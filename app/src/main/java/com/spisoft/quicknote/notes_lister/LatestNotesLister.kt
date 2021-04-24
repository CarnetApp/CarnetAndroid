package com.spisoft.quicknote.notes_lister

import android.content.Context
import com.spisoft.quicknote.databases.CacheManager
import com.spisoft.quicknote.databases.RecentHelper
import java.util.*

class LatestNotesLister(private val context: Context):NotesLister {
    override fun getNotes(): List<Any> {
        val latest = RecentHelper.getInstance(context).cachedLatestNotes
        for (i in latest.indices) {
            val curNote = latest[i]
            val note = CacheManager.getInstance(context)[curNote.path]
            if (note != null) {
                note.isPinned = curNote.isPinned
                latest[i] = note
            }
        }
        return ArrayList<Any>(latest)
    }

}