package com.spisoft.quicknote.editor.recorder

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.MediaRecorder
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.spisoft.quicknote.server.HttpServer
import com.spisoft.sync.Log
import omrecorder.*
import org.apache.commons.lang3.StringEscapeUtils
import top.oply.opuslib.OpusConverter
import top.oply.opuslib.OpusEvent
import java.io.File
import java.util.*


public class AudioRecorderJS (ct: Activity, server: HttpServer, webview:WebView) {



    private val ct: Activity = ct
    private val webview: WebView = webview
    private val server: HttpServer = server
    private var recorder: Recorder ?= null;
    inner class MyEventSender:OpusEvent(ct){

        override fun sendEvent(eventType: Int) {
            sendEvent(eventType, null);
        }

        override fun sendEvent(eventType: Int, msg: String?) {
            if(eventType == OpusEvent.CONVERT_STARTED){

            }
            else if (eventType == OpusEvent.CONVERT_FINISHED){
                File(ct.cacheDir,"/tmpaudio.wav").delete()

                webview.post {
                    webview.loadUrl("javascript:AndroidRecorder.instance.onEncodingEnd()")
                    webview.loadUrl("javascript:AndroidRecorder.instance.onFileReady('" + StringEscapeUtils.escapeEcmaScript(server.getUrl("/api/note/open/0/getMedia/"+File(msg!!).name)) + "')")
                }
            }
        }
    }

    @JavascriptInterface
    public fun start(channels: String, bitrate:String, sampleRate:String) {
        Log.d("AudioRecorderJS", "starting");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(ct.getApplicationContext(),
                        android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) run {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(ct,
                    arrayOf(android.Manifest.permission.RECORD_AUDIO),
                    1301)

        } else
            startRecording(channels, bitrate, sampleRate)

    }

    private fun startRecording(channels: String, bitrate:String, sampleRate:String) {
        File(ct.cacheDir,"/tmpaudio.wav").delete()
        File(ct.cacheDir,"/tmpaudio.wav").parentFile.mkdirs()
        recorder = OmRecorder.wav(
                PullTransport.Default(PullableSource.Default(
                        AudioRecordConfig.Default(
                                MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                                AudioFormat.CHANNEL_IN_STEREO, 44100
                        )
                )), File(ct.cacheDir,"/tmpaudio.wav"))
        recorder!!.startRecording();
        webview.post {
            webview.loadUrl("javascript:AndroidRecorder.instance.setState('recording')")
            webview.loadUrl("javascript:AndroidRecorder.instance.onstart()")
        }
    }
    fun onRequestPermissionsResult(requestCode: Int,
                                   permissions: Array<String>, grantResults: IntArray): Boolean {
        when (requestCode) {
            1301 -> {
                Log.d("WebView", "PERMISSION FOR AUDIO")
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecording("", "", "");

                } else {

                }
                return true
            }
        }
        return false
    }

    @JavascriptInterface
    public fun pause() {
        recorder!!.pauseRecording();
        webview.post {
            webview.loadUrl("javascript:AndroidRecorder.instance.setState('pause')")
            webview.loadUrl("javascript:AndroidRecorder.instance.onpause()")
        }
    }

    @JavascriptInterface
    public fun resume() {
        recorder!!.resumeRecording();
        webview.post {
            webview.loadUrl("javascript:AndroidRecorder.instance.setState('recording')")
            webview.loadUrl("javascript:AndroidRecorder.instance.onresume()")
        }
    }

    @JavascriptInterface
    public fun stop() {
        recorder!!.stopRecording();
        Date().toLocaleString()
        OpusConverter.getInstance().setEventSender(MyEventSender())

        val opusName = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss a",Date()).toString()+".opus"
        val out = File(ct.cacheDir,"/currentnote/data/"+opusName)
        out.parentFile.mkdirs()
        webview.post {
            webview.loadUrl("javascript:AndroidRecorder.instance.onstop()")
            webview.loadUrl("javascript:AndroidRecorder.instance.onEncodingStart()")
        }
        OpusConverter.getInstance().encode( File(ct.cacheDir,"/tmpaudio.wav").absolutePath,  out.absolutePath, "")
        //
    }
}