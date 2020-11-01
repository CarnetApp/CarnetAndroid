package com.spisoft.quicknote.databases;

import android.content.Context;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.sync.utils.FileLocker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by alexandre on 03/02/16.
 */
public class RecentHelper {
    private static final String TAG = "RecentHelper";
    private static RecentHelper sRecentHelper;
    private final Context mContext;
    public static final String RECENT_FOLDER_NAME = "recentdb";
    private static final int CURRENT_VERSION=1;
    private final String mPath;
    private ArrayList<Note> mCachedLatestNotes;
    private String mCurrentJsonStr;
    private List<Note> mPinnedNotes;

    private RecentHelper(Context context, String path){
        mContext = context;
        mPath = path;
    }
    public static RecentHelper getInstance(Context context){
        if(sRecentHelper == null){
            sRecentHelper = new RecentHelper(context, NoteManager.getDontTouchFolder(context)+"/"+ RECENT_FOLDER_NAME+"/"+PreferenceHelper.getUid(context));
        }
        return sRecentHelper;
    }


    public void moveNote(Note note, String path){
        try {
            JSONObject object = getJson();
            JSONObject noteObject = new JSONObject();
            noteObject.put("path",getRelativePath(note.path, mContext));
            noteObject.put("newPath",getRelativePath(path, mContext));
            noteObject.put("action","move");
            noteObject.put("time",System.currentTimeMillis());
            object.getJSONArray("data").put(noteObject);
            write(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void renameNote(Note note, String newName){
        File notFile = new File(note.path);
        File toFile = new File(notFile.getParentFile(), newName);
        moveNote(note, toFile.getAbsolutePath());
    }
    public void addNote(Note note){
        try {
            JSONObject object = getJson();
            JSONObject noteObject = new JSONObject();
            noteObject.put("path",getRelativePath(note.path, mContext));
            noteObject.put("action","add");
            noteObject.put("time",System.currentTimeMillis());
            object.getJSONArray("data").put(noteObject);
            write(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void removeRecent(Note note){
        try {
            JSONObject object = getJson();
            JSONObject noteObject = new JSONObject();
            noteObject.put("path",getRelativePath(note.path, mContext));
            noteObject.put("action","remove");
            noteObject.put("time",System.currentTimeMillis());
            object.getJSONArray("data").put(noteObject);
            write(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void unpin(Note note){
        try {
            JSONObject object = getJson();
            JSONObject noteObject = new JSONObject();
            noteObject.put("path",getRelativePath(note.path, mContext));
            noteObject.put("action","unpin");
            noteObject.put("time",System.currentTimeMillis());
            object.getJSONArray("data").put(noteObject);
            write(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void pin(Note note){
        try {
            JSONObject object = getJson();
            JSONObject noteObject = new JSONObject();
            noteObject.put("path",getRelativePath(note.path, mContext));
            noteObject.put("action","pin");
            noteObject.put("time",System.currentTimeMillis());
            object.getJSONArray("data").put(noteObject);
            write(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void moveNote(Note note, int pos){


    }

    private synchronized void write(String s) {
        mCurrentJsonStr = s;
        synchronized (FileLocker.getLockOnPath(getRecentPath())) {
            File file = new File(getRecentPath());
            FileWriter fw = null;
            if (!file.exists()) {

                file.getParentFile().mkdirs();
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                fw = new FileWriter(getRecentPath(), false);
                fw.append(s + "\n");
                fw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            reloadCache();
        }
    }

    private synchronized String read(){
        if(mCurrentJsonStr != null && false) //disabling cache
            return mCurrentJsonStr;
        synchronized (FileLocker.getLockOnPath(getRecentPath())) {
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
            try {
                br = new BufferedReader(new FileReader(getRecentPath()));
                String line = br.readLine();
                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null)
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
            mCurrentJsonStr = sb.toString();
        }
        return mCurrentJsonStr;
    }
    private String getRecentPath() {
        return mPath;
    }
    public static String getRelativePath(String absolute, Context context){
        String path  = PreferenceHelper.getRootPath(context)+"/";
        if(absolute.startsWith(path))
          return absolute.substring(path.length());
        else
            return absolute;

    }

    public List<Note> getCachedLatestNotes(){
        if(mCachedLatestNotes == null)
            reloadCache();
        return mCachedLatestNotes;
    }

    public List<Note> getPinnedNotes(){
        if(mPinnedNotes == null)
            reloadCache();
        return mPinnedNotes;
    }

    public void reloadCache(){
        CacheManager.getInstance(mContext).loadCache();
        List<Note> notes = new ArrayList<>();
        List<Note> pin = new ArrayList<>();

        String rootPath = PreferenceHelper.getRootPath(mContext)+"/";
        try {
            JSONObject object = getJson();
            for(int i = 0; i<object.getJSONArray("data").length(); i++){
                JSONObject obj = object.getJSONArray("data").getJSONObject(i);
                String action = obj.getString("action");
                String path = obj.getString("path");
                Note note = new Note(rootPath + path);
                if(action.equals("add")){
                    notes.remove(note);
                    notes.add(0,note);
                }else if(action.equals("pin")){
                    pin.remove(note);
                    note.isPinned = true;
                    pin.add(0,note);
                }else if(action.equals("unpin")){
                    pin.remove(note);
                }
                else if(action.equals("remove")){
                    notes.remove(note);
                    pin.remove(note);
                } else if(action.equals("move")){
                    int index = -1;
                    if((index = notes.indexOf(note))>=0){
                        notes.get(index).setPath(rootPath+obj.getString("newPath"));
                    }
                    if((index = pin.indexOf(note))>=0){
                        ((Note)pin.get(index)).setPath(rootPath+obj.getString("newPath"));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<Note> toReturn = new ArrayList<>(pin);
        for(Note note : notes){
            if(!toReturn.contains(note)) {
                toReturn.add(note);
            }
        }
        mPinnedNotes = pin;
        mCachedLatestNotes = toReturn;
    }


    public JSONObject getJson() throws JSONException {

        String jsonString = read();
        if(jsonString==null||jsonString.isEmpty())
            jsonString = "{\"data\":[]}";
        return new JSONObject(jsonString);
    }

    private void update(int version) {

    }

    public void renameDirectory(String absolutePath, String absolutePathTo) {

    }

    public boolean mergeDB(JSONObject otherDBJson){

        boolean hasChanged = false;
        try {
            JSONObject myJSON = getJson();

            for(int i = 0; i<otherDBJson.getJSONArray("data").length(); i++){
                JSONObject obj = otherDBJson.getJSONArray("data").getJSONObject(i);
                String action = obj.getString("action");
                String path = obj.getString("path");
                long time = obj.getLong("time");
                boolean isIn = false;
                int index = 0;
                for(int j = 0; j<myJSON.getJSONArray("data").length(); j++){
                    JSONObject myObj = myJSON.getJSONArray("data").getJSONObject(j);
                    String myAction = myObj.getString("action");
                    String myPath = myObj.getString("path");
                    long myTime = myObj.getLong("time");
                    if(myAction.equals(action) && myPath.equals(path) && myTime == time){
                        isIn = true;
                        break;
                    }
                    if(myTime<time){
                        index = j;
                    }
                }
              //  Log.d(TAG,"merging :"+path+" isIn ? "+isIn);
                if(!isIn){
                    hasChanged = true;
                    myJSON.getJSONArray("data").put(obj);
                }
            }

            if(hasChanged) {
                //sorting

                ArrayList<JSONObject> array = new ArrayList<JSONObject>();
                for (int i = 0; i < myJSON.getJSONArray("data").length(); i++) {
                    try {
                        array.add(myJSON.getJSONArray("data").getJSONObject(i));
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                Collections.sort(array, new Comparator<JSONObject>() {

                    @Override
                    public int compare(JSONObject lhs, JSONObject rhs) {
                        // TODO Auto-generated method stub

                        try {
                            return (lhs.getString("time").compareTo(rhs.getString("time")));
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            return 0;
                        }
                    }
                });
                myJSON.put("data", new JSONArray(array));
                if(hasChanged)
                    write(myJSON.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return hasChanged;

    }

    public boolean mergeDB(String otherDBPath){
        RecentHelper otherDBHelper = new RecentHelper(mContext, otherDBPath);
        JSONObject otherDBJson = null;
        try {
            otherDBJson = otherDBHelper.getJson();
            return mergeDB(otherDBJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }


}
