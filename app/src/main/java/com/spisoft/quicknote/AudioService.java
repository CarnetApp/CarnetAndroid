package com.spisoft.quicknote;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.webkit.MimeTypeMap;

import com.spisoft.sync.Log;

import java.io.File;
import java.io.FileDescriptor;
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


    public AudioService(){
        sAudioService = this;

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
        sNote = note;
        sMedia = media;
        reset();
        Log.d("audiodebug","setMedia", Log.LEVEL_ALWAYS_LOG);


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
            return;
        }
        try {
            Log.d("audiodebug","play", Log.LEVEL_ALWAYS_LOG);

            mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(mHttpServer.getUrl()));
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            sIsPlaying = true;
            sIsPaused = false;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("audiodebug","exxcept "+e.getMessage(), Log.LEVEL_ALWAYS_LOG);

        }

    }

    public void pause(){
       mMediaPlayer.pause();
       sIsPlaying = false;
       sIsPaused = true;
    }

    public void stop(){
        mMediaPlayer.stop();
        sIsPlaying = false;
        sIsPaused = false;
    }

    public void reset(){
        if(sIsPlaying)
            stop();
        sIsPlaying = false;
        sIsPaused = false;

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
            Log.d("audiodebug","method "+method, Log.LEVEL_ALWAYS_LOG);

            if (Method.GET.equals(method)) {
                try {
                    ZipFile zp = new ZipFile(sNote.path);
                    ZipEntry ze = zp.getEntry(sMedia);
                    InputStream is = zp.getInputStream(ze);
                    Log.d("audiodebug","asking media "+ ze +" "+ is, Log.LEVEL_ALWAYS_LOG);

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
