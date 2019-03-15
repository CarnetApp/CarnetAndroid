package com.spisoft.quicknote.browser

import android.content.Context
import android.util.AttributeSet

import com.spisoft.quicknote.editor.EditorView

class WebBasedView : EditorView {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun getUrl(): String {
        return "/reader/index.html"
    }
}
