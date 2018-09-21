package com.spisoft.quicknote.updater;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EditorUpdater implements Updater{

    private static final String TAG = "EditorUpdater";

    private void copyReader(Context ct) {
        //copy reader to separate folder and change rootpath
        String rootPath = ct.getFilesDir().getAbsolutePath();
        Log.d(TAG, "copying reader");
        copyFileOrDir(ct, rootPath,"reader/reader/reader.html");
        String reader = FileUtils.readFile(rootPath + "/reader/reader/reader.html");
        FileUtils.writeToFile(rootPath + "/tmp/reader.html", reader.replace("<!ROOTPATH>", "../reader/"));
        String firstLine = reader.substring(0,reader.indexOf("\n"));
        Log.d(TAG,"archive version "+firstLine);
        int version = Integer.parseInt(firstLine.substring("<!--".length(), firstLine.length()-"-->".length()));
        int currentVersion = PreferenceHelper.getCurrentReaderVersion(ct);
        Log.d(TAG,"current version "+currentVersion);
        Log.d(TAG,"archive version "+version);
        File dir = new File(rootPath + "/reader");
        if (dir.exists())
            FileUtils.deleteRecursive(dir);
        dir.mkdirs();
        copyFileOrDir(ct, rootPath,"reader");
        PreferenceHelper.setCurrentReaderVersion(ct, version);

    }



    public void copyFileOrDir(Context ct, String rootPath, String path) {
        Log.d("assetdebug", "copy " + path);
        AssetManager assetManager = ct.getAssets();
        String assets[] = null;
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(ct,rootPath, path);
            } else {
                String fullPath = rootPath + "/" + path;
                File dir = new File(fullPath);
                if (!dir.exists())
                    dir.mkdirs();
                for (int i = 0; i < assets.length; ++i) {
                    copyFileOrDir(ct, rootPath, path + "/" + assets[i]);
                }
            }
        } catch (IOException ex) {
            Log.e("tag", "I/O Exception", ex);
        }
    }

    private void copyFile(Context ct, String rootPath, String filename) {
        AssetManager assetManager = ct.getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            String newFileName = rootPath + "/" + filename;
            new File(newFileName).getParentFile().mkdirs();
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    @Override
    public void update(Context ct, int oldVersion, int newVersion) {
        copyReader(ct);
    }
}
