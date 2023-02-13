package com.spisoft.quicknote.updater;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.R;

import static android.view.View.GONE;

public class UpdaterActivity extends AppCompatActivity {

    private static final String TAG = "UpdaterActivity";
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            if(msg.what == 0){
                if(msg.arg1 <=0)
                    end();
                else{
                    ((Button)findViewById(R.id.update_button)).setText(getResources().getString(R.string.skip)+" ("+msg.arg1+")");
                    Message nmsg = new Message();
                    nmsg.what = 0;
                    nmsg.arg1 = msg.arg1 - 1;
                    sendMessageDelayed(nmsg,1000);
                }
            }
        }

    };
    private ChangelogFragment mChangelogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updater);
        mChangelogFragment = (ChangelogFragment)getSupportFragmentManager().getFragments().get(0);
        mChangelogFragment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mHandler.removeMessages(0);
                ((Button)findViewById(R.id.update_button)).setText(R.string.skip);
                return false;
            }
        });
        findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeMessages(0);
                ((Button)findViewById(R.id.update_button)).setText(R.string.skip);
            }
        });

        if(getOldVersion(this) == -1    ){
            findViewById(R.id.changelog_fragment).setVisibility(View.GONE);
            findViewById(R.id.first_start).setVisibility(View.VISIBLE);
        }
        else{
            findViewById(R.id.changelog_fragment).setVisibility(View.VISIBLE);
            findViewById(R.id.first_start).setVisibility(View.GONE);
        }
        findViewById(R.id.update_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                end();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        startUpdate();
    }
    private void end(){
        mHandler.removeMessages(0);
        setResult(RESULT_OK);
        finish();
    }
    private void startUpdate() {
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                int oldVersion = getOldVersion(UpdaterActivity.this);
                int newVersion = getNewVersion(UpdaterActivity.this);
                new EditorUpdater().update(UpdaterActivity.this, oldVersion, newVersion);
                PreferenceManager.getDefaultSharedPreferences(UpdaterActivity.this).edit().putInt(PreferenceHelper.APP_VERSION_PREF, newVersion).commit();
                return null;
            }

            @Override
            protected void onPostExecute(Void result){
                if(findViewById(R.id.changelog_fragment).getVisibility() == View.VISIBLE){
                    findViewById(R.id.update_button).setEnabled(true);
                    findViewById(R.id.progressBar).setVisibility(GONE);
                    Message msg = new Message();
                    msg.what = 0;
                    msg.arg1 = 5;
                    mHandler.sendMessage(msg);
                }else
                    end();
            }
        }.execute();
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if(findViewById(R.id.update_button).isEnabled())
            end();
    }

    public static boolean startUpdateIfNeeded(Activity activity, int requestCode) {
        // start update process


        int oldVersion = getOldVersion(activity);
        int newVersion = getNewVersion(activity);
        Log.d(TAG, "current version "+oldVersion+" new "+newVersion);

        if(newVersion != oldVersion || true){

            activity.startActivityForResult(new Intent(activity, UpdaterActivity.class),requestCode);
            return true;
        }
        return false;
    }

    public static int getNewVersion(Context ct){
        int version = -1;
        try {
            PackageInfo pInfo = ct.getPackageManager().getPackageInfo(ct.getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public static int getOldVersion(Context ct){
        return PreferenceManager.getDefaultSharedPreferences(ct).getInt(PreferenceHelper.APP_VERSION_PREF, -1);
    }
}
