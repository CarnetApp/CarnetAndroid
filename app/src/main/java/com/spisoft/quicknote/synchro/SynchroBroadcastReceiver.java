package com.spisoft.quicknote.synchro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by phoenamandre on 30/04/16.
 */
public class SynchroBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, SynchroService.class));
    }
}
