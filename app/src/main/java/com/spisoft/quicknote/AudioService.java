package com.spisoft.quicknote;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.util.ServerRunner;


public class AudioService extends Service {
    public static Note sNote;
    public static String sMedia;
    public boolean sIsPlaying;
    public boolean sIsPaused;

    public static AudioService sAudioService;
    private AudioHttpServer mHttpServer;
    private MediaPlayer mMediaPlayer;
    private StatusListener mStatusListener;


    public AudioService(){
        sAudioService = this;

    }

    public void setListener(StatusListener statusListener) {
        mStatusListener = statusListener;
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
        } else if(sIsPaused){
            mMediaPlayer.start();
            sIsPlaying = true;
            sIsPaused = false;
            mStatusListener.onStatusChange(sNote, sMedia, sIsPlaying);
            return;
        }
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(mHttpServer.getUrl()));
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            sIsPlaying = true;
            sIsPaused = false;
            mStatusListener.onStatusChange(sNote, sMedia, sIsPlaying);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void pause(){
       mMediaPlayer.pause();
       sIsPlaying = false;
       sIsPaused = true;
       mStatusListener.onStatusChange(sNote, sMedia, sIsPlaying);
    }

    public void stop(){
        mMediaPlayer.stop();
        sIsPlaying = false;
        sIsPaused = false;
        mStatusListener.onStatusChange(sNote, sMedia, sIsPlaying);
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
