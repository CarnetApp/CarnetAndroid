package com.spisoft.quicknote.databases;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.synchro.SynchroService;
import com.spisoft.quicknote.synchro.googledrive.DriveWrapper;

import java.io.File;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DBMergerService extends JobService {
    static final String TAG = "DBMergerService";
    private static final int JOB_ID = 1000;
    private static boolean sIsRunning = false;
    public DBMergerService() {
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        sIsRunning = true;
                Log.d(TAG, "starting merging task");
                File recentDBFolder = new File(NoteManager.getDontTouchFolder(DBMergerService.this)+"/"+ RecentHelper.RECENT_FOLDER_NAME);
                if(recentDBFolder.exists()){
                    RecentHelper myRecentHelper = RecentHelper.getInstance(DBMergerService.this);
                    File[] dbs = recentDBFolder.listFiles();
                    if(dbs!=null){
                        for(File db : dbs){
                            if(!PreferenceHelper.getUid(DBMergerService.this).equals(db.getName())){
                                Log.d(TAG, "merging with "+db.getName());

                                myRecentHelper.mergeDB(db.getAbsolutePath());
                            }
                        }
                    }
                }

                scheduleJob(DBMergerService.this);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        sIsRunning = false;
        return true;
    }

    public static boolean isJobScheduledOrRunning(Context context){
        if(sIsRunning)return true;
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        for(JobInfo jobInfo : jobScheduler.getAllPendingJobs()){
            if (jobInfo.getId() == JOB_ID)
                return true;
        }
        return false;
    }

    public static void scheduleJob(Context context) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "scheduleJob");
            ComponentName serviceComponent = new ComponentName(context, DBMergerService.class);
            JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceComponent);
            builder.setMinimumLatency(5*60 * 1000); // wait at least
            builder.setOverrideDeadline(10*60 * 1000); // maximum delay

            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());

        }
    }
}
