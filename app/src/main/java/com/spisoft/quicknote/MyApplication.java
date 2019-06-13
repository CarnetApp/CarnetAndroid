package com.spisoft.quicknote;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;

import com.spisoft.quicknote.browser.NoteListFragment;
import com.spisoft.quicknote.databases.CacheManager;
import com.spisoft.quicknote.databases.DBMergerService;
import com.spisoft.quicknote.reminders.RemindersManager;
import com.spisoft.quicknote.synchro.AccountConfigActivity;
import com.spisoft.sync.Configuration;
import com.spisoft.sync.Log;
import com.spisoft.sync.utils.Utils;
import com.spisoft.sync.wrappers.WrapperFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandre on 22/02/16.
 */
public class MyApplication extends Application implements Configuration.PathObserver {

    private static final String TAG = "MyApplication";

    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(base);
        if(PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceHelper.ROOT_PATH_PREFERENCE,null) == null){
            if(PreferenceManager.getDefaultSharedPreferences(this).getInt(PreferenceHelper.LAUNCH_COUNT, 0)>1) {
                // has already been launched, but back old root path
                PreferenceHelper.setRootPath(this, PreferenceHelper.OLD_DEFAULT_ROOT_PATH);
            } else
                PreferenceHelper.setRootPath(this, new File(this.getExternalFilesDir(null), "notes").getAbsolutePath());
        }
        Utils.context = this;
        Log.isDebug = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_debug_log",BuildConfig.DEBUG);
        MultiDex.install(this);
        Log.d("uiddebug",PreferenceHelper.getUid(this));

        Configuration.sOnAccountSelectedListener = new Configuration.OnAccountSelectedListener() {
            @Override
            public void onAccountSelected(int accountId, int accountType) {
                startAccountConfigActivity(accountId, accountType);
            }
        };
        Configuration.sOnAccountCreatedListener = new Configuration.OnAccountCreatedListener() {
            @Override
            public void onAccountCreated(int accountId, int accountType) {
                startAccountConfigActivity(accountId, accountType);
            }
        };
        Configuration.dontDisplayNotification = false;
        Configuration.icon = R.mipmap.ic_launcher_foreground;
        Configuration.addPathObserver(PreferenceHelper.getRootPath(this), this);
        try {
            WrapperFactory.wrappers.add(Class.forName("com.spisoft.gsync.wrappers.googledrive.GDriveWrapper"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        DBMergerService.setListeners(this);
    }
    private void startAccountConfigActivity(int accountId, int accountType){
        Intent intent = new Intent(MyApplication.this, AccountConfigActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AccountConfigActivity.EXTRA_ACCOUNT_ID, accountId);
        intent.putExtra(AccountConfigActivity.EXTRA_ACCOUNT_TYPE, accountType);
        startActivity(intent);
    }

    @Override
    public void onPathChanged(final String path, final List<String> modifiedPaths) {
        Log.d(TAG, "onPathChanged "+path);
        new AsyncTask<Void, Void, ArrayList<Note>>(){

            @Override
            protected ArrayList<Note> doInBackground(Void... voids) {
                boolean hasAddedSmt = false;
                ArrayList<Note> notes = new ArrayList<>();
                for (String filepath : modifiedPaths){
                    if(filepath.endsWith(".sqd")){
                        Log.d(TAG, "onPathChanged "+filepath);

                        CacheManager.getInstance(MyApplication.this).loadCache();//won't load twice
                        if(new File(filepath).exists()){
                            CacheManager.getInstance(MyApplication.this).addToCache(filepath);
                            RemindersManager.Companion.getInstance(MyApplication.this).add(CacheManager.getInstance(MyApplication.this).get(filepath));
                        }
                        else {
                            CacheManager.getInstance(MyApplication.this).removeFromCache(filepath);
                            RemindersManager.Companion.getInstance(MyApplication.this).remove(filepath);
                        }
                        hasAddedSmt = true;
                        notes.add(new Note(filepath));
                    }
                }
                if(hasAddedSmt)
                    CacheManager.getInstance(MyApplication.this).writeCache();

                return notes;
            }
            @Override
            protected void onPostExecute(ArrayList<Note> result) {
                Intent intent = new Intent(NoteListFragment.ACTION_RELOAD);
                intent.putExtra("notes", result);
                sendBroadcast(intent);

            }
        }.execute();

    }
}
