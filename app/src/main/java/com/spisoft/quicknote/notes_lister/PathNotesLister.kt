package com.spisoft.quicknote.notes_lister

import android.content.Context
import com.spisoft.quicknote.Note
import com.spisoft.quicknote.PreferenceHelper
import com.spisoft.quicknote.databases.CacheManager
import java.io.File
import java.util.*

class PathNotesLister(private val path: String, private val context:Context, private val includeDirectories:Boolean):NotesLister {
    override fun getNotes():List<Any> {
        val avoidDbFolder = path == PreferenceHelper.getRootPath(context)
        val file = File(path)
        val ret: MutableList<Any> = ArrayList()
        val dir: MutableList<Any> = ArrayList()
        val notes: MutableList<Any> = ArrayList()
        if (file.exists()) {
            val files: Array<File> = file.listFiles()
            if (files != null) {
                for (file1 in files) {
                    if (file1.name.startsWith(".") || avoidDbFolder && file1.name == "quickdoc") continue
                    if (file1.name.endsWith(".sqd")) {
                        var note = CacheManager.getInstance(context)[file1.absolutePath]
                        if (note == null) {
                            note = Note(file1.absolutePath)
                        }
                        notes.add(note)
                    } else if (file1.isDirectory && includeDirectories) dir.add(file1)
                }
            }
            ret.addAll(dir)
            ret.addAll(notes)
        }
        return ret;
    }
}