package com.spisoft.quicknote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by alexandre on 11/02/16.
 */
public class HomeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(FloatingService.sService!=null){
            FloatingService.sService.requestMinimize();
        }
    }
}
