package com.spisoft.quicknote.databases;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.util.Log;

import com.spisoft.quicknote.MainActivity;
import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.browser.NoteListFragment;
import com.spisoft.sync.Configuration;
import com.spisoft.sync.utils.Utils;

import java.io.File;
import java.util.List;

public class DBMergerService {
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
            Log.d(TAG, "on recent changed " + path);
            if (path.contains("/recentdb"))
                scheduleJob(Utils.context, true, RECENT_DATABASE);
            else
                scheduleJob(Utils.context, true, KEYWORDS_DATABASE);

        }
    };
    private static PreferenceHelper.RootPathChangeListener sRootPathListener = new PreferenceHelper.RootPathChangeListener() {
        @Override
        public void onRootPathChangeListener(String oldPath, String newPath) {
            Configuration.addPathObserver(NoteManager.getDontTouchFolder(Utils.context) + "/" + RecentHelper.RECENT_FOLDER_NAME, sRecentPathObserver);
            Configuration.addPathObserver(NoteManager.getDontTouchFolder(Utils.context) + "/" + KeywordsHelper.KEYWORDS_FOLDER_NAME, sRecentPathObserver);
        }
    };

    public static void setListeners(Context context){
        if (!isListenerSet) {
            Configuration.addPathObserver(NoteManager.getDontTouchFolder(context) + "/" + RecentHelper.RECENT_FOLDER_NAME, sRecentPathObserver);
            Configuration.addPathObserver(NoteManager.getDontTouchFolder(Utils.context) + "/" + KeywordsHelper.KEYWORDS_FOLDER_NAME, sRecentPathObserver);
            PreferenceHelper.getInstance(context).addOnRootPathChangedListener(sRootPathListener);
            isListenerSet = true;
        }
    }
    public static void scheduleJob(Context context, boolean now, int database) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DBMergerJobService.scheduleJob(context, now, database);
        }
        else
            DBMergerIntentService.scheduleJob(context, now, database);
    }
    public static boolean isJobScheduledOrRunning(Context context) {
        if (sIsRunning) return true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return DBMergerJobService.isJobScheduledOrRunning(context);
        }
        return false;
    }

    public static class CommonDBMerger{
        private final Handler mHandler;
        private final Context mContext;

        public CommonDBMerger(Context context){
            mHandler = new Handler();
            mContext = context;
        }

        public void startMerging(int database){
            if (database == ALL_DATABASES || database == RECENT_DATABASE) {
                File recentDBFolder = new File(NoteManager.getDontTouchFolder(mContext) + "/" + RecentHelper.RECENT_FOLDER_NAME);
                boolean hasChanged = false;
                if (recentDBFolder.exists()) {
                    RecentHelper myRecentHelper = RecentHelper.getInstance(mContext);
                    File[] dbs = recentDBFolder.listFiles();
                    if (dbs != null) {
                        for (File db : dbs) {
                            if (!PreferenceHelper.getUid(mContext).equals(db.getName())) {
                                Log.d(TAG, "merging with " + db.getName());

                                if (myRecentHelper.mergeDB(db.getAbsolutePath()))
                                    hasChanged = true;
                            }
                        }
                    }
                }
                if (hasChanged)
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Utils.context.sendBroadcast(new Intent(NoteListFragment.ACTION_RELOAD));
                        }
                    });
            }
            if (database == ALL_DATABASES || database == KEYWORDS_DATABASE) {
                Log.d(TAG, "merging keywords");
                boolean hasChanged = false;
                File recentDBFolder = new File(NoteManager.getDontTouchFolder(mContext) + "/" + KeywordsHelper.KEYWORDS_FOLDER_NAME);
                if (recentDBFolder.exists()) {
                    KeywordsHelper myRecentHelper = KeywordsHelper.getInstance(mContext);
                    File[] dbs = recentDBFolder.listFiles();
                    if (dbs != null) {
                        for (File db : dbs) {
                            if (!PreferenceHelper.getUid(mContext).equals(db.getName())) {
                                Log.d(TAG, "merging with " + db.getName());

                                if (myRecentHelper.mergeDB(db.getAbsolutePath()))
                                    hasChanged = true;
                            }
                        }
                    }
                }
                if (hasChanged)
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Utils.context.sendBroadcast(new Intent(MainActivity.ACTION_RELOAD_KEYWORDS));
                        }
                    });
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static class DBMergerJobService extends JobService {
        private final CommonDBMerger mCommonDBMerger;

        public DBMergerJobService() {
            mCommonDBMerger = new CommonDBMerger(this);
        }

        @Override
        public boolean onStartJob(final JobParameters jobParameters) {
            sIsRunning = true;
            new Thread(){
                public void run(){
                    Log.d(TAG, "starting merging task");
                    int database = jobParameters.getExtras().getInt(EXTRA_DATABASE, ALL_DATABASES);
                    mCommonDBMerger.startMerging(database);
                    sIsRunning = false;

                }
            }.start();

            return true;
        }

        @Override
        public boolean onStopJob(JobParameters jobParameters) {
            return true;
        }
        public static boolean isJobScheduledOrRunning(Context context) {
            if (sIsRunning) return true;
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            for (JobInfo jobInfo : jobScheduler.getAllPendingJobs()) {
                if (jobInfo.getId() == JOB_ID)
                    return true;
            }
            return false;
        }

        public static void scheduleJob(Context context, boolean now, int database) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.d(TAG, "scheduleJob");
                ComponentName serviceComponent = new ComponentName(context, DBMergerJobService.class);
                JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceComponent);
                builder.setMinimumLatency(now ? 100 : 20 * 60 * 1000); // wait at least
                builder.setOverrideDeadline(now ? 1000 : 50 * 60 * 1000); // maximum delay
                PersistableBundle bundle = new PersistableBundle();
                bundle.putInt(EXTRA_DATABASE, database);
                builder.setExtras(bundle);
                JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                jobScheduler.cancel(JOB_ID);
                jobScheduler.schedule(builder.build());

            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static class DBMergerIntentService extends IntentService {
        private CommonDBMerger mCommonDBMerger;

        public DBMergerIntentService(String name) {
            super(name);
            mCommonDBMerger = new CommonDBMerger(this);

        }

        public DBMergerIntentService(){
            this("DBMergerIntentService");
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            sIsRunning = true;
            Log.d(TAG, "starting merging task");
            int database = intent.getExtras().getInt(EXTRA_DATABASE, ALL_DATABASES);
            mCommonDBMerger.startMerging(database);
            sIsRunning = false;
        }



        public static void scheduleJob(Context context, boolean now, int database) {
            if(now){
                Intent intent = new Intent(context, DBMergerIntentService.class);
                intent.putExtra(EXTRA_DATABASE, database);
                context.startService(intent);
            }
        }
    }
}