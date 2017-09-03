package com.spisoft.quicknote.synchro.googledrive;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by alexandre on 25/04/16.
 */
public class GoogleDriveAccountHelper {

    private static GoogleDriveAccountHelper sGoogleDriveAccountHelper;
    private final Context mContext;

    public GoogleDriveAccountHelper(Context ct) {
        mContext = ct;
    }

    public static GoogleDriveAccountHelper getInstance(Context ct) {
        if(sGoogleDriveAccountHelper==null)
            sGoogleDriveAccountHelper = new GoogleDriveAccountHelper(ct);
        return sGoogleDriveAccountHelper;
    }

    public static class GoogleAccount{
        public String rootFolder;
        public String rootPath;
        public GoogleApiClient googleApiClient;
    }

    public GoogleAccount getGoogleAccount(long accountID) {
        return null;
    }
}
