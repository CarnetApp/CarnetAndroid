package com.spisoft.quicknote;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

import static android.app.NotificationManager.IMPORTANCE_LOW;


public class AudioService extends Service implements MediaPlayer.OnCompletionListener {
    private static final int NOTIFICATION_ID = 4;
    public static Note sNote;
    public static String sMedia;
    public boolean sIsPlaying;
    public boolean sIsPaused;

    public static AudioService sAudioService;
    private AudioHttpServer mHttpServer;
    private MediaPlayer mMediaPlayer;
    private StatusListener mStatusListener;
    private String mChannelId = "";


    public AudioService(){
        sAudioService = this;

    }

    public void setListener(StatusListener statusListener) {
        mStatusListener = statusListener;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        sIsPlaying = false;
        sIsPaused = false;
        mMediaPlayer.reset();
        mStatusListener.onStatusChange(sNote, sMedia, sIsPlaying);
        stopForeground(true);
    }

    public interface StatusListener{
        void onStatusChange(Note note, String media, boolean isPlaying);
    }

    public class LocalBinder extends Binder {
        AudioService getService() {
            return AudioService.this;
        }
    }

    public static boolean isPlaying(){
        return sAudioService != null && sAudioService.sIsPlaying;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }


    public void setMedia(Note note, String media){
        if(sNote != null && sIsPlaying)
            stop();
        sNote = note;
        sMedia = media;
        reset();
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        int code = super.onStartCommand(intent, flags, startId);
        if(intent != null && "pause".equals(intent.getAction())&& isPlaying())
            pause();

        return code;
    }
    public void toggleMedia(Note note, String media){

        if(note.equals(sNote) && media.equals(sMedia)){
            if(sIsPlaying)
                pause();
            else
                play();
        }
        else {
            setMedia(note, media);
            play();
        }

    }

    public void play(){
        if(mHttpServer == null){
            mHttpServer = new AudioHttpServer();
        }
        if(mMediaPlayer == null){
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(this);
        } else if(sIsPaused){
            mMediaPlayer.start();
            sIsPlaying = true;
            sIsPaused = false;
            mStatusListener.onStatusChange(sNote, sMedia, sIsPlaying);
            startNotification();
            return;
        }
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(mHttpServer.getUrl()));
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            sIsPlaying = true;
            sIsPaused = false;
            mStatusListener.onStatusChange(sNote, sMedia, sIsPlaying);
            startNotification();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void startNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O&&mChannelId.isEmpty()) {
            mChannelId = createNotificationChannel("audio", getString(R.string.audio_player));
        }
        Intent intent = new Intent(this, AudioService.class);
        intent.setAction("pause");
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, mChannelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentText(sMedia)
                .addAction(new NotificationCompat.Action.Builder(R.drawable.pause_black, getString(R.string.pause),
                        PendingIntent.getService(this, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT)).build())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_STATUS)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName){
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_LOW);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    public void pause(){
       mMediaPlayer.pause();
       sIsPlaying = false;
       sIsPaused = true;
       mStatusListener.onStatusChange(sNote, sMedia, sIsPlaying);
       stopForeground(true);
    }

    public void stop(){
        mMediaPlayer.stop();
        sIsPlaying = false;
        sIsPaused = false;
        mStatusListener.onStatusChange(sNote, sMedia, sIsPlaying);
        stopForeground(true);
    }

    public void reset(){
        if(sIsPlaying)
            stop();
        if(mMediaPlayer != null)
            mMediaPlayer.reset();
        sIsPlaying = false;
        sIsPaused = false;
        mStatusListener.onStatusChange(sNote, sMedia, sIsPlaying);

    }
    class AudioHttpServer extends NanoHTTPD {


        public AudioHttpServer() {
            super(0);
            ServerRunner.executeInstance(this);
            try {
                start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public Response serve(IHTTPSession session) {
            Method method = session.getMethod();

            if (Method.GET.equals(method)) {
                try {
                    File noteFile = new File(sNote.path);
                    InputStream is = null;
                    if(noteFile.exists()){
                        if(noteFile.isDirectory()){
                            is = new FileInputStream(new File(noteFile, sMedia));
                        }
                        else {
                            ZipFile zp = new ZipFile(sNote.path);
                            ZipEntry ze = zp.getEntry(sMedia);
                            is = zp.getInputStream(ze);
                        }
                    }


                    return  NanoHTTPD.newChunkedResponse(Response.Status.OK,
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(sMedia)),is);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        public String getUrl(){
            int port = getListeningPort();
            String url = "http://localhost:"+port+"/"+new File(sMedia).getName();
            return url;
        }
    }
}
