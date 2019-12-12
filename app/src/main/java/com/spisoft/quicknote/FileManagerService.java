package com.spisoft.quicknote;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

import com.spisoft.quicknote.browser.NoteListFragment;
import com.spisoft.quicknote.databases.RecentHelper;
import com.spisoft.sync.utils.FileLocker;
import com.spisoft.quicknote.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandre on 12/05/16.
 */
public class FileManagerService extends Service {

    public static final String LIST_FROM_FROM = "list_string_from";
    public static final String TARGET_FOLDER = "target_folder";
    public static final String ACTION_COPY_ENDS = "action_copy_ends";
    public static boolean sIsCopying;

    public FileManagerService() {
    }

    @Override
    public int  onStartCommand(final Intent intent, int flags, int ded){
        int ret = super.onStartCommand(intent, flags, ded);

        if(!sIsCopying) {
            sIsCopying = true;
            new Thread() {
                public void run(){
                    List<String> sources = intent.getStringArrayListExtra(LIST_FROM_FROM);
                    String target = intent.getStringExtra(TARGET_FOLDER);
                for( String source  :sources)

                {
                    synchronized (FileLocker.getLockOnPath(source)) {

                        if (FloatingService.sService != null && FloatingService.sService.getNote() != null && FloatingService.sService.getNote().path.equals(source))
                            continue;
                        File targetFile = new File(target, new File(source).getName());
                        synchronized (FileLocker.getLockOnPath(targetFile.getAbsolutePath())) {
                            if (FileUtils.moveDirectoryOneLocationToAnotherLocation(new File(source), targetFile)) {
                                if (source.endsWith(".sqd"))
                                    RecentHelper.getInstance(FileManagerService.this).moveNote(new Note(source), targetFile.getAbsolutePath());
                            }

                        }
                    }
                }

                sendBroadcast(new Intent(ACTION_COPY_ENDS));

                sendBroadcast(new Intent(NoteListFragment.ACTION_RELOAD));

                stopSelf();

                sIsCopying=false;
            }
            }.start();
        }
        return ret;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void startCopy(Context context,ArrayList<Object> sources, String target){
        ArrayList<String>sourcesPath = new ArrayList<>();
        for(Object source : sources){
            if(source instanceof File)
                sourcesPath.add(((File) source).getAbsolutePath());
            else if(source instanceof Note)
                sourcesPath.add(((Note) source).path);
        }
        Intent intent = new Intent(context, FileManagerService.class);
        intent.putExtra(LIST_FROM_FROM, sourcesPath);
        intent.putExtra(TARGET_FOLDER, target);
        context.startService(intent);

    }
}
