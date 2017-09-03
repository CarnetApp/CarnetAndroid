package com.spisoft.quicknote.synchro;

import android.content.Context;

import java.io.File;

/**
 * Created by alexandre on 25/04/16.
 */
public abstract class Wrapper {
    public static final int ERROR = -1;
    public static final int STATUS_SUCCESS = 0;
    protected final Context mContext;
    protected final long mAccountID;

    public abstract int loadDistantFiles();
    public Wrapper(Context ct, long accountID){
        mAccountID = accountID;
        mContext = ct;
    }

    public abstract int onFile(File file, String relativePath, String md5, boolean fileIsBeingEdited);

    public abstract void endOfSync();

    /*need to be created in static */
    //public abstract boolean isMyAccount(int accountType);
}
