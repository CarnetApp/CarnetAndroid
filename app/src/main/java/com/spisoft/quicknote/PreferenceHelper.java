package com.spisoft.quicknote;

import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.utils.Utils;
import com.spisoft.sync.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by alexandre on 03/02/16.
 */
public class PreferenceHelper {

    public static final String DEFAULT_ROOT_PATH = new File(Environment.getExternalStorageDirectory(),"QuickNote").getAbsolutePath();
    public static final String ROOT_PATH_PREFERENCE = "pref_root_path";
    public static final String CURRENT_PASSWORD_PREFERENCE = "pref_password";
    public static final String DISPLAY_AD_COUNT = "pref_display_ad_count";
    public static final String PASSWORD_ON_MINIMIZE_PREFERENCE = "pref_password_on_minimize";
    public static final String REMOVE_AD_FREE = "pref_remove_ad_free";
    public static final String NOTE_VERSION_PREF = "pref_note_version";
    public static final String PREF_UID = "pref_uid";
    private static PreferenceHelper sPreferenceHelper;
    private final Context mContext;
    private List<RootPathChangeListener> mRootPathChangeListener = new ArrayList<>();

    public void addOnRootPathChangedListener(RootPathChangeListener sRootPathListener) {
        mRootPathChangeListener.add(sRootPathListener);
    }

    public interface RootPathChangeListener{
        public void onRootPathChangeListener(String oldPath, String newPath);
    }
    public PreferenceHelper(Context context){
        mContext = context;
    }

    public static PreferenceHelper getInstance(Context context){
        if(sPreferenceHelper == null)
            sPreferenceHelper = new PreferenceHelper(context);
        return sPreferenceHelper;
    }
    public static String getRootPath(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(ROOT_PATH_PREFERENCE, Utils.isDebug(context)?DEFAULT_ROOT_PATH+"Debug":DEFAULT_ROOT_PATH);
    }
    public static void setRootPath(Context context, String rootPath){
        getInstance(context).setRootPath(rootPath);
    }

    public void setRootPath(String rootPath){
        String old = getRootPath(mContext);
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(ROOT_PATH_PREFERENCE, rootPath).commit();
        for(RootPathChangeListener listener : mRootPathChangeListener)
            listener.onRootPathChangeListener(old, rootPath);
    }

    public static boolean shouldLockOnMinimize(Context ct) {
        return PreferenceManager.getDefaultSharedPreferences(ct).getBoolean(PASSWORD_ON_MINIMIZE_PREFERENCE, false);
    }

    public static boolean shouldDisplayAd(Context context){
        int count = PreferenceManager.getDefaultSharedPreferences(context).getInt(DISPLAY_AD_COUNT, 0);
        return count>1&&!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(REMOVE_AD_FREE, false);
    }
    public static void incrementDisplayAd(Context context){
        int count = PreferenceManager.getDefaultSharedPreferences(context).getInt(DISPLAY_AD_COUNT, 0);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(DISPLAY_AD_COUNT, count+1).commit();
    }
    public static long getLockTime(Context ct) {
        return 3000;
    }

    public static boolean shouldLockOnBubbleStart(Context ct) {
        return false;
    }

    public static String getPassword(Context ct) {
        return PreferenceManager.getDefaultSharedPreferences(ct).getString(CURRENT_PASSWORD_PREFERENCE, "0000");
    }
    public static void setPassword(Context ct, String password) {
        PreferenceManager.getDefaultSharedPreferences(ct).edit().putString(CURRENT_PASSWORD_PREFERENCE, password).commit();
    }

    public static void setShouldAskPasswordOnMinimize(Context ct, boolean b) {
        PreferenceManager.getDefaultSharedPreferences(ct).edit().putBoolean(PASSWORD_ON_MINIMIZE_PREFERENCE, b).commit();
    }

    public static int getCurrentNoteVersion(Context ct) {
        return PreferenceManager.getDefaultSharedPreferences(ct).getInt(NOTE_VERSION_PREF, NoteManager.NEW_VERSION);
    }

    public static void setCurrentNoteVersion(Context ct, int newVersion) {
        PreferenceManager.getDefaultSharedPreferences(ct).edit().putInt(NOTE_VERSION_PREF, newVersion).commit();
    }

    public static String getUid(Context context) {
        String uid = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_UID,"");
        if(uid.isEmpty()){
            uid = UUID.randomUUID().toString();
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_UID, uid).commit();
        }
        return uid;
    }

}
