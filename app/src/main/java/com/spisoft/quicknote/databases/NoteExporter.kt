package com.spisoft.quicknote.databases

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.spisoft.quicknote.PreferenceHelper
import com.spisoft.quicknote.R
import com.spisoft.quicknote.utils.FileUtils
import com.spisoft.quicknote.utils.ZipUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class NoteExporter : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(NoteExporter, "start")
        AlertDialog.Builder(this)
        val exportIntent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        exportIntent.addCategory(Intent.CATEGORY_OPENABLE)
        exportIntent.type = "application/zip"
        exportIntent.putExtra(Intent.EXTRA_TITLE, "carnet_archive.zip")
        startActivityForResult(exportIntent, 1000)

        Log.d(NoteExporter, "end")
    }
    override fun onStart() {
        super.onStart()


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            val uri = data.data
            if (uri != null) {
                try {

                        CoroutineScope(Dispatchers.Main).launch {
                            // runs on UI thread
                            val dialog = AlertDialog.Builder(this@NoteExporter).setMessage(R.string.exporting).show()
                            dialog.setCancelable(false)
                            startWriteZip(uri)
                            Log.d(NoteExporter, "end1")
                            dialog.dismiss()
                            finish()

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        else finish()
    }



    suspend fun writeZip(uri: Uri) = Dispatchers.Default {
        val result: String = ""
        // make network call
        Log.d(NoteExporter, "getResult")
        val tmp = File(applicationContext.getCacheDir(), "tmpfile")
        ZipUtils.zipFolder(File(PreferenceHelper.getRootPath(applicationContext)), FileOutputStream(tmp),ArrayList<String>())
        FileUtils.copy(FileInputStream(tmp), contentResolver.openOutputStream(uri))

        Log.d(NoteExporter, "getResult2")

        return@Default result
    }

    // note the suspend modifier next to the fun keyword
    suspend fun startWriteZip(uri: Uri) {
        // the getResults() function should be a suspend function too, more on that later
        Log.d(NoteExporter, "makeNetworkCall")

        val result = writeZip(uri)
        Log.d(NoteExporter, "makeNetworkCall2")

    }

    companion object {
        val NoteExporter = "NoteExporter"
    }
}