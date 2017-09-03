package com.spisoft.quicknote.synchro;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.browser.NoteListFragment;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.databases.RecentHelper;
import com.spisoft.quicknote.editor.EditorView;
import com.spisoft.quicknote.synchro.googledrive.DriveWrapper;
import com.spisoft.quicknote.utils.FileUtils;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by alexandre on 25/04/16.
 */
public class SynchroService extends Service{
    private static final String TAG = "SynchroService";
    private static final int ALARM_ID = 1001;
    private static final long REPEAT = 10*60*1000;
    private Thread mSyncThread;
    private DriveWrapper mGoogleDriveWrapper;
    private int SUCCESS = 0;
    private int ERROR = -1;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //first get all distant files
    //get All local files with needed info


    //first case : new local file

    /*

        iterate throw local files
        give each one of them to wrapper
        wrapper compare to db : not in db, not sync ? compare with online, not sync : sync


     */

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        int ret = super.onStartCommand(intent,flags, startId);
        if (mSyncThread == null || !mSyncThread.isAlive()) {
            mGoogleDriveWrapper = new DriveWrapper(this,0);
            mSyncThread = new SyncThread();
            mSyncThread.start();


        }
        return ret;
    }



    private class SyncThread extends Thread {

        public void run(){
            Log.d(TAG,"starting sync at "+ DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis())));
            if(mGoogleDriveWrapper.connect()!=Wrapper.STATUS_SUCCESS)
                return;
            if(mGoogleDriveWrapper.loadRootFolder()!=Wrapper.STATUS_SUCCESS)
                return;
                if(mGoogleDriveWrapper.loadDistantFiles()!=Wrapper.STATUS_SUCCESS)
                    return;
            recursiveSync(new File(PreferenceHelper.getRootPath(SynchroService.this)));
            mGoogleDriveWrapper.endOfSync();
            AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);

            Intent intent = new Intent(SynchroService.this, SynchroService.class);
            PendingIntent alarmIntent = PendingIntent.getService(SynchroService.this, ALARM_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmMgr.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+REPEAT,  alarmIntent);
            sendBroadcast(new Intent(NoteListFragment.ACTION_RELOAD));

        }

        private int recursiveSync(File file) {
            if(file.isDirectory()){
                if(!file.getAbsolutePath().
                        equals(NoteManager.getDontTouchFolder(getApplicationContext()))) {
                    Log.d(TAG,"folder detected "+file.getAbsolutePath());

                    File[] files = file.listFiles();
                    if (files != null) {
                        for (File f : files)
                            if(recursiveSync(f)!=SUCCESS)
                                return ERROR;
                    }
                }
            }
            else if(file.getName().endsWith(NoteManager.EXTENSION)){
                Log.d(TAG,"file detected "+file.getAbsolutePath());
                //sync
                String md5 = FileUtils.md5(file.getAbsolutePath());
                if(mGoogleDriveWrapper.onFile(file, RecentHelper.getRelativePath(file.getAbsolutePath(),SynchroService.this), md5, file.getAbsolutePath().equals(EditorView.editedAbsolutePath))!=Wrapper.STATUS_SUCCESS)
                    return ERROR;
            }
            return SUCCESS;

        }
    }
}
