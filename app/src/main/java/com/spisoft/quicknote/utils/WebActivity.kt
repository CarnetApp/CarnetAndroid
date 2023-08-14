package com.spisoft.quicknote.utils

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.spisoft.quicknote.R
import com.spisoft.quicknote.editor.EditorView

class WebActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        val webFragment = WebFragment.newInstance(intent.getStringExtra(WebFragment.ARG_URL)!!)
        supportFragmentManager.beginTransaction().replace(R.id.root, webFragment).commit();
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (fragment in supportFragmentManager.fragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }
}
