package com.spisoft.quicknote.editor.recorder

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.webkit.JavascriptInterface
import android.media.AudioFormat.CHANNEL_IN_MONO
import android.media.AudioFormat.ENCODING_PCM_16BIT
import android.media.MediaRecorder
import android.media.MediaRecorder.AudioSource.MIC
import android.os.Bundle
import android.webkit.WebView
import com.spisoft.quicknote.databases.RecentHelper
import com.spisoft.quicknote.server.HttpServer
import com.spisoft.sync.Log
import omrecorder.*
import org.apache.commons.lang3.StringEscapeUtils
import top.oply.opuslib.OpusConverter
import top.oply.opuslib.OpusEvent
import java.io.File
import java.text.DateFormat
import java.util.*




public class AudioRecorderJS (ct: Context, server: HttpServer, webview:WebView) {



    private val ct: Context = ct
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
                    webview.loadUrl("javascript:AndroidRecorder.instance.setState('none')")
                    webview.loadUrl("javascript:AndroidRecorder.instance.onstop()")
                    webview.loadUrl("javascript:AndroidRecorder.instance.onFileReady('" + StringEscapeUtils.escapeEcmaScript(server.getUrl("/api/note/open/0/getMedia/"+File(msg!!).name)) + "')")
                }
            }
        }
    }

    @JavascriptInterface
    public fun start(channels: String, bitrate:String, sampleRate:String) {
        Log.d("AudioRecorderJS", "starting");
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

        OpusConverter.getInstance().encode( File(ct.cacheDir,"/tmpaudio.wav").absolutePath,  out.absolutePath, "")
        //
    }
}