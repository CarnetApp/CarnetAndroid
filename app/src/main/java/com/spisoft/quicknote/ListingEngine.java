package com.spisoft.quicknote;

import android.os.Handler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by phoenamandre on 07/02/16.
 */
public class ListingEngine {

    private final File mUri;
    private final ListingListener mListener;
    private String mExt;
    private boolean mOnlyDirectory;
    private Handler mHandler = new Handler();
    public interface ListingListener{
        public void onFileList(List<File> list);
    }


    
    public ListingEngine(File uri, ListingListener listener){
        mListener = listener;
        mUri = uri;
    }
    public void setFilter(boolean onlyDirectories, String ext){
        mExt = ext;
        mOnlyDirectory = onlyDirectories;
    }
    public void list(){
        new Thread(){
            public void run(){
                File file = mUri;
                File[] files = file.listFiles();
                final List<File> fileList = new ArrayList<>();
                if(files!=null){
                    for (File f : files){

                        if(!mOnlyDirectory||f.isDirectory()) {
                            if(f.isDirectory()||mExt==null||mExt.isEmpty()|| f.getName().endsWith("."+mExt))
                            fileList.add(f);

                        }

                    }
                }
                Collections.sort(fileList);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onFileList(fileList);
                    }
                });

            }
        }.start();
    }
}
