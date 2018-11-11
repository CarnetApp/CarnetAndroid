package com.spisoft.quicknote.updater;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.spisoft.quicknote.MainActivity;
import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.R;
import com.spisoft.quicknote.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UpdaterActivity extends AppCompatActivity {

    private static final String TAG = "UpdaterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updater);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startUpdate();
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
                setResult(RESULT_OK);
                finish();
            }
        }.execute();
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        return;
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
