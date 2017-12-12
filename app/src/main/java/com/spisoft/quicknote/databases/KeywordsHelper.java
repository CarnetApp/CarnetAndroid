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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alexandre on 12/12/17.
 */

public class KeywordsHelper {
    private static KeywordsHelper sKeywordsHelper;
    private final String mPath;
    private static final String KEYWORDS_FOLDER_NAME = "keywords";
    private final Context mContext;

    private KeywordsHelper(Context context, String path){
        mContext = context;
        mPath = path;
    }
    public static KeywordsHelper getInstance(Context context){
        if(sKeywordsHelper == null){
            sKeywordsHelper = new KeywordsHelper(context, NoteManager.getDontTouchFolder(context)+"/"+ KEYWORDS_FOLDER_NAME+"/"+ PreferenceHelper.getUid(context));
        }
        return sKeywordsHelper;
    }

    public void addKeyword(String keyword, Note note){
        try {
            JSONObject object = getJson();
            JSONObject noteObject = new JSONObject();
            noteObject.put("keyword",keyword);
            noteObject.put("path",RecentHelper.getRelativePath(note.path, mContext));
            noteObject.put("action","add");
            noteObject.put("time",System.currentTimeMillis());
            object.getJSONArray("data").put(noteObject);
            write(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void removeKeyword(String keyword, Note note){
        try {
            JSONObject object = getJson();
            JSONObject noteObject = new JSONObject();
            noteObject.put("keyword",keyword);
            noteObject.put("path",RecentHelper.getRelativePath(note.path, mContext));
            noteObject.put("action","remove");
            noteObject.put("time",System.currentTimeMillis());
            object.getJSONArray("data").put(noteObject);
            write(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void write(String s) {
        File file= new File (mPath);
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
            fw = new FileWriter(mPath,false);
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
            br = new BufferedReader(new FileReader(mPath));
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

    private JSONObject getJson() throws JSONException {
        Log.d("jsondebug", "getJson");

        String jsonString = read();
        if(jsonString==null||jsonString.isEmpty())
            jsonString = "{\"data\":[]}";
        return new JSONObject(jsonString);
    }


    public Map<String,List<String>> getFlattenDB(int limit){
        Map<String,List<String>> flattenDb = new HashMap<>();
        List<Object> notes = new ArrayList<>();
        try {
            JSONObject object = getJson();
            for(int i = 0; i<object.getJSONArray("data").length(); i++){
                JSONObject obj = object.getJSONArray("data").getJSONObject(i);
                String action = obj.getString("action");
                String keyword = obj.getString("keyword");
                String path = obj.getString("path");
                Note note = new Note(PreferenceHelper.getRootPath(mContext)+"/"+path);
                if(action.equals("add")){
                    if(!flattenDb.containsKey(keyword))
                        flattenDb.put(keyword, new ArrayList<String>());
                    if(!flattenDb.get(keyword).contains(path))
                        flattenDb.get(keyword).add(path);
                }
                else if(action.equals("remove")){
                    if(flattenDb.containsKey(keyword) && flattenDb.get(keyword).contains(path))
                        flattenDb.get(keyword).remove(path);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return flattenDb;
    }
}
