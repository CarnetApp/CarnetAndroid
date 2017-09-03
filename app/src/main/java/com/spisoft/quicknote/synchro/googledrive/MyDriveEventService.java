package com.spisoft.quicknote.synchro.googledrive;

import android.util.Log;

import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.DriveEventService;

/**
 * Created by phoenamandre on 30/04/16.
 */
public class MyDriveEventService extends DriveEventService {

    private static final String TAG = "MyDriveEventService";

    @Override
    public void onChange(ChangeEvent event) {
        Log.d(TAG, event.toString());


        // Application-specific handling of event.
    }

}