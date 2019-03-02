package com.spisoft.quicknote.databases;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.spisoft.quicknote.MainActivity;
import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.browser.NoteListFragment;
import com.spisoft.sync.Configuration;
import com.spisoft.sync.utils.Utils;

import java.io.File;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DBMergerService extends JobService {
    static final String TAG = "DBMergerService";
    private static final int JOB_ID = 1000;
    private static final java.lang.String EXTRA_DATABASE = "extra_database";
    public static final int ALL_DATABASES = -1;
    private static boolean sIsRunning = false;
    public static boolean isListenerSet = false;
    public static final int RECENT_DATABASE = 0;
    public static final int KEYWORDS_DATABASE = 1;
    private static Configuration.PathObserver sRecentPathObserver = new Configuration.PathObserver() {
        @Override
        public void onPathChanged(String path, List<String> modifiedPaths) {
            Log.d(TAG, "on recent changed "+path);
            if(path.contains("/recentdb"))
                scheduleJob(Utils.context, true, RECENT_DATABASE);
            else
                scheduleJob(Utils.context, true, KEYWORDS_DATABASE);

        }
    };
    private static PreferenceHelper.RootPathChangeListener sRootPathListener = new PreferenceHelper.RootPathChangeListener() {
        @Override
        public void onRootPathChangeListener(String oldPath, String newPath) {
            Configuration.addPathObserver(NoteManager.getDontTouchFolder(Utils.context)+"/"+ RecentHelper.RECENT_FOLDER_NAME, sRecentPathObserver);
            Configuration.addPathObserver(NoteManager.getDontTouchFolder(Utils.context)+"/"+ KeywordsHelper.KEYWORDS_FOLDER_NAME, sRecentPathObserver);
        }
    };
    private final Handler mHandler;



    public DBMergerService() {
        mHandler = new Handler();
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        sIsRunning = true;
                Log.d(TAG, "starting merging task");
        int database = jobParameters.getExtras().getInt(EXTRA_DATABASE,ALL_DATABASES);
        if(database == ALL_DATABASES || database == RECENT_DATABASE) {
            File recentDBFolder = new File(NoteManager.getDontTouchFolder(DBMergerService.this) + "/" + RecentHelper.RECENT_FOLDER_NAME);
            boolean hasChanged = false;
            if (recentDBFolder.exists()) {
                RecentHelper myRecentHelper = RecentHelper.getInstance(DBMergerService.this);
                File[] dbs = recentDBFolder.listFiles();
                if (dbs != null) {
                    for (File db : dbs) {
                        if (!PreferenceHelper.getUid(DBMergerService.this).equals(db.getName())) {
                            Log.d(TAG, "merging with " + db.getName());

                            if(myRecentHelper.mergeDB(db.getAbsolutePath()))
                                hasChanged = true;
                        }
                    }
                }
            }
            if(hasChanged)
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Utils.context.sendBroadcast(new Intent(NoteListFragment.ACTION_RELOAD));
                }
            });
        }
        if(database == ALL_DATABASES || database == KEYWORDS_DATABASE) {
            Log.d(TAG, "merging keywords");
            boolean hasChanged = false;
            File recentDBFolder = new File(NoteManager.getDontTouchFolder(DBMergerService.this) + "/" + KeywordsHelper.KEYWORDS_FOLDER_NAME);
            if (recentDBFolder.exists()) {
                KeywordsHelper myRecentHelper = KeywordsHelper.getInstance(DBMergerService.this);
                File[] dbs = recentDBFolder.listFiles();
                if (dbs != null) {
                    for (File db : dbs) {
                        if (!PreferenceHelper.getUid(DBMergerService.this).equals(db.getName())) {
                            Log.d(TAG, "merging with " + db.getName());

                            if(myRecentHelper.mergeDB(db.getAbsolutePath()))
                                hasChanged = true;
                        }
                    }
                }
            }
            if(hasChanged)
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Utils.context.sendBroadcast(new Intent(MainActivity.ACTION_RELOAD_KEYWORDS));
                }
            });
        }
        scheduleJob(DBMergerService.this, false, ALL_DATABASES);

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
    public static void scheduleJob(Context context, boolean now, int database) {
        if(!isListenerSet){
            Configuration.addPathObserver(NoteManager.getDontTouchFolder(context)+"/"+ RecentHelper.RECENT_FOLDER_NAME, sRecentPathObserver);
            Configuration.addPathObserver(NoteManager.getDontTouchFolder(Utils.context)+"/"+ KeywordsHelper.KEYWORDS_FOLDER_NAME, sRecentPathObserver);
            PreferenceHelper.getInstance(context).addOnRootPathChangedListener(sRootPathListener);
        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "scheduleJob");
            ComponentName serviceComponent = new ComponentName(context, DBMergerService.class);
            JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceComponent);
            builder.setMinimumLatency(now?100:5*60 * 1000); // wait at least
            builder.setOverrideDeadline(now?1000:10*60 * 1000); // maximum delay
            PersistableBundle bundle  =new PersistableBundle();
            bundle.putInt(EXTRA_DATABASE, database);
            builder.setExtras(bundle);
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.cancel(JOB_ID);
            jobScheduler.schedule(builder.build());

        }
    }
}
