package com.spisoft.quicknote.databases;

import android.content.Context;
import android.util.Log;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.PreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandre on 03/02/16.
 */
public class RecentHelper {

    private static RecentHelper sRecentHelper;
    private final Context mContext;
    private static final String RECENT_FOLDER_NAME = "recentdb";
    private static final int CURRENT_VERSION=1;

    private RecentHelper(Context context){
        mContext = context;
    }
    public static RecentHelper getInstance(Context context){
        if(sRecentHelper == null){
            sRecentHelper = new RecentHelper(context);
        }
        return sRecentHelper;
    }


    public void moveNote(Note note, String path){


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

    public void moveNote(Note note, int pos){


    }

    private void write(String s) {
        File file= new File (getRecentPath());
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
            fw = new FileWriter(getRecentPath(),false);
            fw.append(s + "\n");
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String read(){
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
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(br!=null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return sb.toString();
    }
    private String getRecentPath() {
        return NoteManager.getDontTouchFolder(mContext)+"/"+ RECENT_FOLDER_NAME+"/"+PreferenceHelper.getUid(mContext);
    }
    public static String getRelativePath(String absolute, Context context){
        String path  = PreferenceHelper.getRootPath(context)+"/";
        if(absolute.startsWith(path))
          return absolute.substring(path.length());
        else
            return absolute;

    }
    private JSONObject noteToJsonObject(Note note) {
        JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("relative_path",getRelativePath(note.path, mContext));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        return jsonObject;
    }

    public List<Object> getLatestNotes(int limit){
        List<Object> notes = new ArrayList<>();
        try {
            JSONObject object = getJson();
            for(int i = 0; i<object.getJSONArray("data").length(); i++){
                JSONObject obj = object.getJSONArray("data").getJSONObject(i);
                String action = obj.getString("action");
                String path = obj.getString("path");
                Note note = new Note(PreferenceHelper.getRootPath(mContext)+"/"+path);
                if(action.equals("add")){
                    notes.remove(note);
                    notes.add(0,note);
                    Log.d("addingdebug","adding "+note.path);
                }
                else if(action.equals("remove")){
                    notes.remove(note);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return notes;
    }

    private JSONObject getJson() throws JSONException {
        Log.d("jsondebug", "getJson");

        String jsonString = read();
        if(jsonString==null||jsonString.isEmpty())
            jsonString = "{\"data\":[]}";
        return new JSONObject(jsonString);
    }

    private void update(int version) {

    }

    public void renameDirectory(String absolutePath, String absolutePathTo) {

    }
}
