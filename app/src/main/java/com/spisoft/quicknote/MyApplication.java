package com.spisoft.quicknote;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.spisoft.quicknote.synchro.AccountConfigActivity;
import com.spisoft.sync.Configuration;
import com.spisoft.sync.utils.Utils;

/**
 * Created by alexandre on 22/02/16.
 */
public class MyApplication extends Application implements Configuration.PathObserver {

    private static final String TAG = "MyApplication";

    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(base);
        MultiDex.install(this);
        Log.d("uiddebug",PreferenceHelper.getUid(this));
        Utils.context = this;

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
        Configuration.addPathObserver(PreferenceHelper.getRootPath(this), this);
        Configuration.addPathObserver(PreferenceHelper.getRootPath(this)+"/untitled.sqd", this);
    }
    private void startAccountConfigActivity(int accountId, int accountType){
        Intent intent = new Intent(MyApplication.this, AccountConfigActivity.class);
        intent.putExtra(AccountConfigActivity.EXTRA_ACCOUNT_ID, accountId);
        intent.putExtra(AccountConfigActivity.EXTRA_ACCOUNT_TYPE, accountType);
        startActivity(intent);
    }

    @Override
    public void onPathChanged(String path) {
        Log.d(TAG, "onPathChanged "+path);
    }
}
