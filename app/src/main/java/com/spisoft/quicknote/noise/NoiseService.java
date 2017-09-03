package com.spisoft.quicknote.noise;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.spisoft.quicknote.R;

/**
 * Created by alexandre on 10/03/16.
 */
public class NoiseService extends Service implements SoundPool.OnLoadCompleteListener {

    private static final String ACTION_STOP = "stop_noise";
    public static NoiseService sNoiseService;
    private Noise mNoise;
    private SoundPool mSoundPool;
    private float actVolume;
    private float maxVolume;
    private float volume;
    private int mSoundID = -1;
    private IntentFilter mIntentFilter;
    private BroadcastReceiver mReceiver;

    public void onCreate(){
        super.onCreate();
        Log.d("noisedebug", "onCreate");
        sNoiseService = this;
        mReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                stop();
            }
        };
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ACTION_STOP);
        registerReceiver(mReceiver, mIntentFilter);

        // Load the sounds
        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume = actVolume / maxVolume;
        setNoise(new Noise("file:///android_asset/noises/loud_white_noise.mp3"));
        play();


    }
    public void startForeground(){
        Intent intent = new Intent();
        intent.setAction(NoiseService.ACTION_STOP);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        NotificationCompat.Action.Builder actionBuilder= new NotificationCompat.Action.Builder(R.drawable.settings_icon,getString(R.string.stop), pendingIntent );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(R.drawable.ic_launcher).
                setContentTitle(getString(R.string.app_name)).
                setContentText(getString(R.string.white_noise_player));
        builder.addAction(actionBuilder.build());


        startForeground(4, builder.build());
    }
    public void setNoise(Noise noise){
        mNoise = noise;
        stop();
        mSoundPool.setOnLoadCompleteListener(this);
        mSoundID = mSoundPool.load(this, R.raw.loud_white_noise, 1);

    }

    private void stop() {
        if(mSoundID!=-1)
        mSoundPool.stop(mSoundID);
        stopForeground(true);
    }

    public void play(){
        startForeground();
        mSoundPool.play(mSoundID, volume, volume, 1, -1, 1f);
        Log.d("noisedebug", "play");
    }

    public void pause(){
        mSoundPool.pause(mSoundID);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int i, int i1) {

        Log.d("noisedebug", "setNoise");
        play();
    }

    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
