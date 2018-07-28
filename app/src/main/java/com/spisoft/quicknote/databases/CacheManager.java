package com.spisoft.quicknote.databases;

import android.content.Context;
import android.util.Log;

import com.spisoft.quicknote.Note;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by phiedora on 27/07/18.
 */

public class CacheManager {
    private static final String TAG = "CacheManager";
    private static CacheManager sCacheManager;
    Context mContext;
    File cacheFile = null;
    private HashMap<String, Note> cache;

    public static CacheManager getInstance(Context ct){
        if(sCacheManager == null){
            sCacheManager = new CacheManager(ct);
        }
        return sCacheManager;
    }
    public CacheManager(Context ct){
        mContext = ct;
        cacheFile = new File(mContext.getCacheDir(), "noteinfo.json");
    }

    public void loadCache(){
        if(cache != null ) return;
        cache = new HashMap<>();
        try {
            JSONObject object = getJson();
            JSONArray array = object.getJSONArray("data");
            for(int i =0; i< array.length(); i++){
                JSONObject noteJson = array.getJSONObject(i);
                String path = noteJson.getString("path");
                Note.Metadata metadata = Note.Metadata.fromJSONObject(noteJson.getJSONObject("metadata"));
                Note note = new Note(path);
                note.setMetaData(metadata);
                note.shortText = noteJson.getString("shorttext");
                Log.d(TAG,"adding "+path);
                addToCache(note);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addToCache(Note note){

        cache.put(note.path, note);
    }

    private JSONObject getJson() throws JSONException {
        Log.d("jsondebug", "getJson");

        String jsonString = read();
        if(jsonString==null||jsonString.isEmpty())
            jsonString = "{\"data\":[]}";
        return new JSONObject(jsonString);
    }


    private String read(){
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new FileReader(cacheFile));
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
        Log.d(TAG, "read "+sb.toString());
        return sb.toString();
    }

    public synchronized Note get(String path) {
        Log.d(TAG, "getting "+path);
        return cache.get(path);
    }

    public synchronized void writeCache() {
        JSONObject root = new JSONObject();
        JSONArray data = new JSONArray();
        try {
            root.put("data", data);
            for(Note note : cache.values()){
                JSONObject noteObj = new JSONObject();
                noteObj.put("path", note.path);
                noteObj.put("metadata",note.mMetadata.toJsonObject());
                noteObj.put("shorttext",note.shortText);
                data.put(noteObj);
            }
            write(root.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void write(String s) {
        Log.d(TAG, "write "+s);

        FileWriter fw = null;
        if (!cacheFile.exists()) {

            cacheFile.getParentFile().mkdirs();
            try {
                cacheFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            fw = new FileWriter(cacheFile,false);
            fw.append(s + "\n");
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
