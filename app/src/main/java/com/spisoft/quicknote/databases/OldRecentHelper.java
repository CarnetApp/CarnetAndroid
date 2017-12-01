package com.spisoft.quicknote.databases;

import android.content.Context;
import android.util.Log;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.PreferenceHelper;

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
import java.util.List;

/**
 * Created by alexandre on 03/02/16.
 */
public class OldRecentHelper {

    private static OldRecentHelper sRecentHelper;
    private final Context mContext;
    private static final String RECENT_FILE_NAME = ".recent.db";
    private static final int CURRENT_VERSION=1;

    private OldRecentHelper(Context context){
        mContext = context;
    }
    public static OldRecentHelper getInstance(Context context){
        if(sRecentHelper == null){
            sRecentHelper = new OldRecentHelper(context);
        }
        return sRecentHelper;
    }


    public void moveNote(Note note, String path){
        File toFile = new File(path);
        File notFile = new File(note.path);
        boolean hasChanged = false;
        try {
            JSONObject jsonObj  = getJson();
            if(jsonObj == null) {

                jsonObj = new JSONObject();
                jsonObj.put("version", CURRENT_VERSION);
            }

            JSONArray newArray = new JSONArray();

            if(jsonObj.has("notes")) {
                JSONArray array = jsonObj.getJSONArray("notes");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String relativePath = obj.getString("relative_path");
                    if (relativePath.equals(getRelativePath(toFile.getAbsolutePath(), mContext))) {
                        hasChanged = true;
                    }

                }
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String relativePath = obj.getString("relative_path");

                    if (relativePath.equals(getRelativePath(note.path, mContext)) && !hasChanged) {
                        obj.put("relative_path", getRelativePath(toFile.getAbsolutePath(), mContext));

                    }
                    newArray.put(obj);


                }
            }
            jsonObj.put("notes", newArray);

            write(jsonObj.toString());

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
            JSONObject jsonObj  = getJson();
            if(jsonObj == null) {

                jsonObj = new JSONObject();
                jsonObj.put("version", CURRENT_VERSION);
            }

            JSONArray newArray = new JSONArray();
            boolean shouldAdd = true;
            if(jsonObj.has("notes")) {
                JSONArray array = jsonObj.getJSONArray("notes");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String relativePath = obj.getString("relative_path");

                    if (relativePath.equals(getRelativePath(note.path, mContext)))
                        shouldAdd = false;

                }
            }
            if(shouldAdd)
                newArray.put(noteToJsonObject(note));
            if(jsonObj.has("notes")) {
                JSONArray array = jsonObj.getJSONArray("notes");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String relativePath = obj.getString("relative_path");
                    newArray.put(obj);
                }
            }



            jsonObj.put("notes", newArray);
            write(jsonObj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeRecent(Note note){
        try {
            JSONObject jsonObj  = getJson();
            if(jsonObj == null) {

                jsonObj = new JSONObject();
                jsonObj.put("version", CURRENT_VERSION);
            }
            JSONArray newArray = new JSONArray();
            Log.d("notedebug", "addNote array");
            boolean shouldAdd = true;
            if(jsonObj.has("notes")) {
                JSONArray array = jsonObj.getJSONArray("notes");
                Log.d("notedebug","has array");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    Log.d("notedebug","has note "+obj.getString("relative_path"));
                    String relativePath = obj.getString("relative_path");

                    if (!relativePath.equals(getRelativePath(note.path, mContext)))
                        newArray.put(obj);

                }
            }


            jsonObj.put("notes", newArray);

            write(jsonObj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void moveNote(Note note, int pos){

        try {
            JSONObject jsonObj  = getJson();
            if(jsonObj == null) {

                jsonObj = new JSONObject();
                jsonObj.put("version", CURRENT_VERSION);
            }

            JSONArray newArray = new JSONArray();
            Log.d("notedebug", "addNote array");
            if(jsonObj.has("notes")) {
                JSONArray array = jsonObj.getJSONArray("notes");
                Log.d("notedebug","has array");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    Log.d("notedebug","has note "+obj.getString("relative_path"));
                    String relativePath = obj.getString("relative_path");
                    if(i == pos)
                        newArray.put(noteToJsonObject(note));
                    if (!relativePath.equals(getRelativePath(note.path, mContext)))
                        newArray.put(obj);

                }
            }




            jsonObj.put("notes", newArray);
            Log.d("jsondebug", jsonObj.toString());

            write(jsonObj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        return PreferenceHelper.getRootPath(mContext)+"/"+RECENT_FILE_NAME;
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
            final JSONObject jsonObj = getJson();
            if(jsonObj==null)
                return notes;
            int version = 0;
            version = jsonObj.getInt("version");
            update(version);
            JSONArray array = jsonObj.getJSONArray("notes");
            for (int i = 0; i <array.length(); i++){
                JSONObject obj = array.getJSONObject(i);

                String relativePath = obj.getString("relative_path");
                Log.d("jsondebug", "relativePath");
                Note note = new Note(PreferenceHelper.getRootPath(mContext)+"/"+relativePath);
                notes.add(note);

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
            return null;
        return new JSONObject(jsonString);
    }

    private void update(int version) {

    }

    public void renameDirectory(String absolutePath, String absolutePathTo) {
        if(!absolutePath.endsWith("/"))
            absolutePath +="/";
        if(!absolutePathTo.endsWith("/"))
            absolutePathTo +="/";
        boolean hasChanged = false;
        try {
            JSONObject jsonObj  = getJson();
            if(jsonObj == null) {

                jsonObj = new JSONObject();
                jsonObj.put("version", CURRENT_VERSION);
            }

            JSONArray newArray = new JSONArray();

            if(jsonObj.has("notes")) {
                JSONArray array = jsonObj.getJSONArray("notes");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String relativePath = obj.getString("relative_path");
                    String absoluteNotePath = (PreferenceHelper.getRootPath(mContext)+"/"+relativePath);
                    if (absoluteNotePath.startsWith(absolutePath)) {
                        absoluteNotePath = absolutePathTo+absoluteNotePath.substring(absolutePath.length());
                        obj.put("relative_path", getRelativePath(absoluteNotePath, mContext));
                    }
                    newArray.put(obj);

                }

            }
            jsonObj.put("notes", newArray);

            write(jsonObj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
