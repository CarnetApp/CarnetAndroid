package com.spisoft.quicknote.editor.recorder

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioFormat
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.github.squti.androidwaverecorder.WaveRecorder
import com.spisoft.quicknote.R
import com.spisoft.quicknote.server.HttpServer
import com.spisoft.sync.Log
import org.apache.commons.lang3.StringEscapeUtils
import top.oply.opuslib.OpusConverter
import top.oply.opuslib.OpusEvent
import java.io.File
import java.util.*


public class AudioRecorderJS: Service() {

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): AudioRecorderJS = this@AudioRecorderJS
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("AudioRecorderJS","onBind")

        return binder
    }
    init {
        sService = this;
        Log.d("AudioRecorderJS","create")
    }

    private var mChannelId: String = ""
    private var waveRecorder: WaveRecorder? = null
    private lateinit var ct: Activity
    private lateinit var webview: WebView
    private lateinit var server: HttpServer
    private var audioFile: File ?= null
    inner class MyEventSender:OpusEvent(ct){

        override fun sendEvent(eventType: Int) {
            sendEvent(eventType, null);
        }

        override fun sendEvent(eventType: Int, msg: String?) {
            if(eventType == OpusEvent.CONVERT_STARTED){

            }
            else if (eventType == OpusEvent.CONVERT_FINISHED){
                server.saveNote(server.getCurrentPath())
                File(ct.cacheDir,"/tmpaudio.wav").delete()

                webview.post {
                    webview.loadUrl("javascript:window.AndroidRecorder.onEncodingEnd()")
                    webview.loadUrl("javascript:window.AndroidRecorder.onFileReady('" + StringEscapeUtils.escapeEcmaScript(server.getUrl("/api/note/open/0/getMedia/"+File(msg!!).name)) + "', true)")
                }
                stopForeground(true)
            }
        }
    }

    @JavascriptInterface
    public fun start(channels: String, bitrate:String, sampleRate:String) {
        Log.d("AudioRecorderJS", "starting");
        startNotification(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(ct.getApplicationContext(),
                        android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) run {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(ct,
                    arrayOf(android.Manifest.permission.RECORD_AUDIO),
                    1301)

        } else
            startRecording(channels, bitrate, sampleRate)

    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AudioRecorderJS","onStartCommand")

        val code = super.onStartCommand(intent, flags, startId)
        if (intent != null && "stop" == intent.action && isRecording())
            stop()

        return code
    }

    fun set(ct: Activity, server: HttpServer, webview:WebView){
        this.ct = ct
        this.server = server
        this.webview = webview
    }

    private fun isRecording(): Boolean {
        return true
    }

    fun startNotification(record:Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mChannelId.isEmpty()) {
            mChannelId = createNotificationChannel("audio", getString(R.string.audio_player))
        }
        val intent = Intent(this, AudioRecorderJS::class.java)
        intent.action = "stop"
        val notificationBuilder = NotificationCompat.Builder(this, mChannelId)
         notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)

        if(record) {
            notificationBuilder.addAction(NotificationCompat.Action.Builder(R.drawable.pause_black, getString(R.string.stop),
                    PendingIntent.getService(this, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT)).build())
                    .setContentText(getString(R.string.recording))
        }
        else
            notificationBuilder.setContentText(getString(R.string.encoding))
        val notification = notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_STATUS)
                .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_LOW)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    var js = JSClass()

    inner class JSClass{
        @JavascriptInterface
        public fun pause() {
            this@AudioRecorderJS.pause()
        }

        @JavascriptInterface
        public fun start(channels: String, bitrate:String, sampleRate:String) {
            this@AudioRecorderJS.start(channels, bitrate, sampleRate)

        }

        @JavascriptInterface
        public fun resume() {
            this@AudioRecorderJS.resume()
        }

        @JavascriptInterface
        public fun stop() {
            this@AudioRecorderJS.stop()
        }
    }


    private fun startRecording(channels: String, bitrate:String, sampleRate:String) {

        audioFile = File(ct.cacheDir,"audiotmp.wav")
        TMP_WAV_PATH = audioFile!!.absolutePath
        audioFile!!.delete()
        audioFile!!.parentFile.mkdirs()
        waveRecorder = WaveRecorder(audioFile!!.absolutePath)
        waveRecorder!!.waveConfig.sampleRate = 44100
        waveRecorder!!.waveConfig.channels = AudioFormat.CHANNEL_IN_STEREO
        waveRecorder!!.waveConfig.audioEncoding = AudioFormat.ENCODING_PCM_16BIT
        waveRecorder!!.startRecording()
        webview.post {
            webview.loadUrl("javascript:window.AndroidRecorder.pauseUnavailable()")
            webview.loadUrl("javascript:window.AndroidRecorder.setState('recording')")
            webview.loadUrl("javascript:window.AndroidRecorder.onstart()")
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
        waveRecorder!!.pauseRecording();
        webview.post {
            webview.loadUrl("javascript:window.AndroidRecorder.setState('pause')")
            webview.loadUrl("javascript:window.AndroidRecorder.onpause()")
        }


    }

    @JavascriptInterface
    public fun resume() {
        waveRecorder!!.resumeRecording();
        webview.post {
            webview.loadUrl("javascript:window.AndroidRecorder.setState('recording')")
            webview.loadUrl("javascript:window.AndroidRecorder.onresume()")
        }
    }

    @JavascriptInterface
    public fun stop() {
        waveRecorder!!.stopRecording()
        startNotification(false)
        /* when using js encoder (todo: test)
        webview.post {
             webview.loadUrl("javascript:window.AndroidRecorder.onstop()")
             webview.loadUrl("javascript:window.AndroidRecorder.onFileReady()")
         }*/
        Date().toLocaleString()
        OpusConverter.getInstance().setEventSender(MyEventSender())
        val opusName = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss a",Date()).toString()+".opus"
        val out = File(ct.cacheDir,"/currentnote/data/"+opusName)
        out.parentFile.mkdirs()
        OpusConverter.getInstance().encode( audioFile!!.absolutePath,  out.absolutePath, "")
        webview.post {
            webview.loadUrl("javascript:window.AndroidRecorder.onstop()")
            webview.loadUrl("javascript:window.AndroidRecorder.setState('none')")
            webview.loadUrl("javascript:window.AndroidRecorder.onEncodingStart()")
        }

    }

    companion object {
        @JvmField
        var sService: AudioRecorderJS? = null
        @JvmField
        var TMP_WAV_PATH: String = ""
        var NOTIFICATION_ID = 2998
    }
}