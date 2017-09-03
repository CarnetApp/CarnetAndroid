package com.spisoft.quicknote;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by alexandre on 22/02/16.
 */
public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
