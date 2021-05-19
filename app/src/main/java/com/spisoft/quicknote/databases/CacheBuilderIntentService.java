package com.spisoft.quicknote.databases;

import android.app.IntentService;
import android.content.Intent;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.reminders.RemindersManager;
import com.spisoft.sync.Log;

import java.io.File;
import java.util.ArrayList;

public class CacheBuilderIntentService extends IntentService {
    private static final String TAG = "CacheBuilderIntentService";


    public CacheBuilderIntentService(){
        this("CacheBuilderIntentService");
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public CacheBuilderIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("cachedebug","asking to write");

        CacheManager.getInstance(this).loadCache();
        File file = new File(PreferenceHelper.getRootPath(this));
        explore(file);
        checkDeletedFiles();
        CacheManager.getInstance(this).writeCache();

    }

    private void checkDeletedFiles() {
        for(String path: new ArrayList<>(CacheManager.getInstance(this).getCache().keySet())){
            if(!new File(path).exists())
                CacheManager.getInstance(this).removeFromCache(path);
        }
    }

    private void explore(File file) {
        if(file.getName().endsWith(".sqd")){
            Note note = CacheManager.getInstance(this).get(file.getAbsolutePath());
            if(note == null || note.file_lastmodification != file.lastModified()){
                Log.d(TAG, "building cache for "+file.getName());
                CacheManager.getInstance(this).addToCache(file.getAbsolutePath());
                RemindersManager.Companion.getInstance(this).add(CacheManager.getInstance(this).get(file.getAbsolutePath()));
            } else {
                Log.d(TAG, "is in cache: "+file.getName());
            }
        } else if(file.isDirectory()){
             File [] children = file.listFiles();
             if(children != null){
                 for (File child:children){
                     explore(child);
                 }
             }
         }
    }
}
