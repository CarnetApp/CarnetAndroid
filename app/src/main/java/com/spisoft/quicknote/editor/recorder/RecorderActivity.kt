package com.spisoft.quicknote.editor.recorder


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import java.io.IOException

import java.util.*
import android.R.attr.start
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import com.spisoft.quicknote.R
import kotlinx.android.synthetic.main.activity_recorder.*
import top.oply.opuslib.OpusRecorder
import java.io.File
import java.io.FileOutputStream




class RecorderActivity : AppCompatActivity() {

    private var mIsStarted: Boolean = false
    private val opusRecorder = OpusRecorder.getInstance();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recorder)

        /*button_start_recording.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(this, permissions,0)
            } else {
                startRecording()
            }
        }

        button_stop_recording.setOnClickListener{
            stopRecording()
        }

        button_pause_recording.setOnClickListener {
            pauseRecording()
        }*/
    }

    private fun startRecording(){
        mIsStarted = true
        button_start_recording.setEnabled(false)
        button_stop_recording.setEnabled(true)
        opusRecorder.startRecording("/sdcard/test.opus");
    }

    public fun startRecording(v: View) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)
            ActivityCompat.requestPermissions(this, permissions,0)
        } else {
            startRecording()
        }

    }

    private fun pauseRecording() {

    }

    private fun resumeRecording() {

    }

    public fun stopRecording(v: View){
        opusRecorder.stopRecording();


        button_start_recording.setEnabled(true)
        button_stop_recording.setEnabled(false)

        mIsStarted = false
    }


}