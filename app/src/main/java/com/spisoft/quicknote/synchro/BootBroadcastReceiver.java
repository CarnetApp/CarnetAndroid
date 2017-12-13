package com.spisoft.quicknote.synchro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.spisoft.sync.synchro.SynchroService;
import com.spisoft.quicknote.databases.DBMergerService;

/**
 * Created by phoenamandre on 30/04/16.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //context.startService(new Intent(context, SynchroService.class));
        DBMergerService.scheduleJob(context, true, DBMergerService.ALL_DATABASES);
    }
}