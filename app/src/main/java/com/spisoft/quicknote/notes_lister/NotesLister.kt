package com.spisoft.quicknote.notes_lister

import android.content.Context
import com.spisoft.quicknote.Note

interface NotesLister {

    fun getNotes():List<Any>;
}