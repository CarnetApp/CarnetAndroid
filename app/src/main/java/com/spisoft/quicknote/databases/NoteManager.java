package com.spisoft.quicknote.databases;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.server.ZipReaderAndHttpProxy;
import com.spisoft.quicknote.utils.FileLocker;
import com.spisoft.quicknote.utils.FileUtils;
import com.spisoft.quicknote.utils.ZipUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * Created by phoenamandre on 13/02/16.
 */
public class NoteManager
{

    /*
           project structure
           root/note_title/index.html
           root/note_title/data/



     */
    public static final String ACTION_MOVE = "action_move";
    public static final String OLD_PATH = "old_path";
    public static final String NEW_PATH = "new_path";
    public static final int NEW_VERSION = 2;
    private static final String KEYWORDS = "keywords";
    private static final String TAG = "NoteManager";
    public static String EXTENSION = "sqd";
    public static final String ACTION_UPDATE_END = "update_note_end";
    public static final int PREVIEW_WIDTH = 400;
    public static final int PREVIEW_HEIGHT = 400;


    public static void updateMetadata(final Context context, final Note note){
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                ZipUtils.addEntry(context, note, "metadata.json", new ByteArrayInputStream(note.mMetadata.toJsonObject().toString().getBytes()));

                return null;
            }
        }.execute();
    }

    public static String getHtmlPath(int page){

        String path =  "index.html";

        return path;
    }
    public static String getMetadataPath(){
        return "metadata";
    }



    public static Note createNewNote(String rootPath){
        File rootFile = new File(rootPath);
        String name  = "untitled";
        if(rootFile.exists()){
            File [] children = rootFile.listFiles();
            if(children!=null) {
                boolean found = true;
                int i = 0;
                while (found) {
                    found = false;
                    for(File child : children){
                        if(child.getName().startsWith(name)){
                            found = true;
                            i++;
                            name = "untitled "+i;
                            continue;
                        }
                    }
                }
            }
        }


        String path = rootPath + (!rootPath.endsWith("/")?"/":"")+name+" "+randomChar()+randomChar()+".sqd";
        return new Note(path);
    }

    private static char randomChar () {
        int rnd = (int) (Math.random() * 52);
        char base = (rnd < 26) ? 'A' : 'a';
        return (char) (base + rnd % 26);

    }

    public static String renameNote(Context context,Note note, String newName){
        File toFile = new File(new File(note.path).getParentFile(), newName);
        return moveNote(context, note, toFile.getAbsolutePath());
    }

    public static String moveNote(Context context,Note note, String to){
        File notFile = new File(note.path);
        File toFile = new File(to);
        Log.d(TAG,"renaming to "+to+" "+toFile.exists());
        if(!toFile.exists()){
            notFile.renameTo(toFile);
            RecentHelper.getInstance(context).moveNote(note,toFile.getAbsolutePath());
            KeywordsHelper.getInstance(context).moveNote(note, toFile.getAbsolutePath());
            note.setPath(toFile.getAbsolutePath());
            return toFile.getAbsolutePath();
        }
        return null;
    }

    public static boolean needToUpdate(String path) {
        File file = new File(path);
        return file.exists()&&getNoteVersion(file)< NEW_VERSION;
    }

    public static String getDontTouchFolder(Context ct) {
        return PreferenceHelper.getRootPath(ct)+"/quickdoc";
    }

    public static String getOldDontTouchFolder(Context ct) {
        return PreferenceHelper.getRootPath(ct)+"/.dontouch";
    }

    public static void deleteNote(Context context, Note note) {
        FileUtils.deleteRecursive(new File(note.path));
        RecentHelper.getInstance(context).removeRecent(note);
        KeywordsHelper.getInstance(context).deleteNote(note);
    }

    public interface UpdaterListener{
        public void onUpdateFileList(int length);
        void onUpdate(String name);
        void onUpdateError();
        void onUpdateFinished();
        void onUpdateStart();
    }
    private static SecureRandom random = new SecureRandom();

    public static String nextSessionId() {
        return new BigInteger(8, random).toString(12);
    }
    public static void update(final Context ct, final UpdaterListener listener){
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                if(PreferenceHelper.getCurrentNoteVersion(ct)<NEW_VERSION){
                    File file = new File(PreferenceHelper.getRootPath(ct));
                    PreferenceHelper.setCurrentNoteVersion(ct,NEW_VERSION);
                    synchronized (FileLocker.getLockOnPath(file.getAbsolutePath())) {
                        if (file.exists())
                            FileUtils.copyDirectoryOneLocationToAnotherLocation(file, new File(file.getParentFile(), "QuickNote" + nextSessionId()));
                    }
                    listener.onUpdateStart();
                    if(file.exists())
                        recursiveUpdate(ct,file,listener);

                    listener.onUpdateFinished();
                    ct.sendBroadcast(new Intent(ACTION_UPDATE_END));
                }

                return null;
            }
        }.execute();


    }

    private static void recursiveUpdate(Context ct,File file, UpdaterListener listener) {
        int noteVersion = getNoteVersion(file);
        if(noteVersion !=-1 && noteVersion<NEW_VERSION){
            listener.onUpdate(file.getName());
            updateNote(ct,file, noteVersion);

        }else if(file.isDirectory()&&noteVersion ==-1){
            File []children = file.listFiles();
            if(children!=null){
                for(File child:children)
                    recursiveUpdate(ct,child, listener);
            }
        }

    }

    private static int getNoteVersion(File file) {
        if(file.isDirectory()){
            File[] files = file.listFiles();
            if(files!=null){
                for(File child : files){
                    if(child.getName().equals("index.html"))
                        return 1;
                }
            }
        }
        else if(file.getAbsolutePath().endsWith("sqd"))
            return 2;
        return -1;
    }

    private static boolean updateNote(Context ct,File file, int noteVersion) {
        if(noteVersion==1){
            //just zip
            String path = file.getAbsolutePath();
            if(path.endsWith("/"))
                path = path.substring(0, path.length()-1);
            path+=".sqd";
            if(!new File(path).exists()) {
                if (ZipUtils.zipFolder(file, path, new ArrayList<String>())) {
                    File fileNew = new File(path);
                    if(fileNew.exists()&&fileNew.length()>0) {//triple check
                        FileUtils.deleteRecursive(file);
                        RecentHelper.getInstance(ct).moveNote(new Note(file.getAbsolutePath()), path);
                    }
                }
            }
        }
        return false;
    }

    public static String getDefaultHTML() {
        return "<div id=\"text\" style=\"height:100%;\"> \n<!-- be aware that THIS will be modified in java -->\n<!-- soft won't save note if contains donotsave345oL -->\n<div class=\"edit-zone\" contenteditable></div>\n</div>\n<div id=\"floating\">\n\n</div>";
    }
}
