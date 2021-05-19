package com.spisoft.quicknote.databases;

import android.content.Context;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.browser.NoteInfoRetriever;
import com.spisoft.quicknote.reminders.RemindersManager;
import com.spisoft.sync.Log;

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

    public static synchronized CacheManager getInstance(Context ct){
        if(sCacheManager == null){
            sCacheManager = new CacheManager(ct);
        }
        return sCacheManager;
    }
    public CacheManager(Context ct){
        mContext = ct;
        cacheFile = new File(mContext.getCacheDir(), "noteinfo.json");
    }

    public synchronized void loadCache(){
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
                note.needsUpdateInfo = false;
                note.shortText = noteJson.getString("shorttext");
                if(noteJson.has("lastmodification"))
                    note.file_lastmodification = noteJson.getLong("lastmodification");
                if(noteJson.has("previews")){
                    JSONArray previews = noteJson.getJSONArray("previews");
                    for (int j = 0; j<previews.length(); j++){
                        note.previews.add(previews.getString(j));
                    }
                }
                if(noteJson.has("media")){
                    JSONArray media = noteJson.getJSONArray("media");
                    for (int j = 0; j<media.length(); j++){
                        note.medias.add(media.getString(j));
                    }
                }
                Log.d(TAG,"adding "+path);
                addToCache(note);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public synchronized HashMap<String, Note> getCache(){
        return cache;
    }
    public synchronized void addToCache(Note note){
        if(cache == null ) return;
        note.needsUpdateInfo = false;
        cache.put(note.path, note);
    }

    public synchronized void removeFromCache(String path){
        if(cache == null ) return;
        cache.remove(path);
        RemindersManager.Companion.getInstance(mContext).remove(path);
    }
    public synchronized void addToCache(String notePath){
        if(cache == null ) return;
        File f = new File(notePath);
        if(f.exists()){
            NoteInfoRetriever retriever = new NoteInfoRetriever(new NoteInfoRetriever.NoteInfoListener() {
                @Override
                public void onNoteInfo(Note note) {

                }
            }, mContext);
            Note note = retriever.getNoteInfo(notePath, null, 100);
            cache.put(note.path, note);
        }
    }

    private JSONObject getJson() throws JSONException {

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
        loadCache();
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
                noteObj.put("lastmodification", note.file_lastmodification);
                noteObj.put("metadata",note.mMetadata.toJsonObject());
                noteObj.put("shorttext",note.shortText);
                JSONArray previewsJson = new JSONArray();
                for(String prev : note.previews){
                    previewsJson.put(prev);
                }
                noteObj.put("previews",previewsJson);

                JSONArray mediasJson = new JSONArray();
                for(String media : note.medias){
                    mediasJson.put(media);
                }
                noteObj.put("media",mediasJson);
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

    public void onNoteMoved(String path, String to) {
        Note note = cache.get(path);
        if(note != null){
            cache.remove(path);
            note.path = to;
            cache.put(path, note);
        }
    }
}
