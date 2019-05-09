package com.spisoft.quicknote.databases;

import android.content.Context;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.PreferenceHelper;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alexandre on 12/12/17.
 */

public class KeywordsHelper {
    private static KeywordsHelper sKeywordsHelper;
    private final String mPath;
    public static final String KEYWORDS_FOLDER_NAME = "keywords";
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
    public void moveNote(Note note, String path){
        try {
            JSONObject object = getJson();
            JSONObject noteObject = new JSONObject();
            noteObject.put("path",RecentHelper.getRelativePath(note.path, mContext));
            noteObject.put("newPath",RecentHelper.getRelativePath(path, mContext));
            noteObject.put("action","move");
            noteObject.put("time",System.currentTimeMillis());
            object.getJSONArray("data").put(noteObject);
            write(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void deleteNote(Note note){
        try {
            JSONObject object = getJson();
            JSONObject noteObject = new JSONObject();
            noteObject.put("path",RecentHelper.getRelativePath(note.path, mContext));
            noteObject.put("action","delete");
            noteObject.put("time",System.currentTimeMillis());
            object.getJSONArray("data").put(noteObject);
            write(object.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

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

    public JSONObject getJson() throws JSONException {
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
                String keyword ="";
                if(!action.equals("move")&&!action.equals("delete"))
                    keyword = obj.getString("keyword");
                String path = obj.getString("path");
                if(action.equals("add")){
                    if(!flattenDb.containsKey(keyword))
                        flattenDb.put(keyword, new ArrayList<String>());
                    if(!flattenDb.get(keyword).contains(path))
                        flattenDb.get(keyword).add(path);
                }
                else if(action.equals("remove")){
                    if(flattenDb.containsKey(keyword) && flattenDb.get(keyword).contains(path))
                        flattenDb.get(keyword).remove(path);
                } else if(action.equals("move")){
                    for(Map.Entry<String, List<String>> entry : flattenDb.entrySet()){
                        int index = -1;
                        if((index = entry.getValue().indexOf(path))>=0){
                            entry.getValue().set(index, obj.getString("newPath"));
                        }
                    }
                }
                else if(action.equals("delete")){
                    for(Map.Entry<String, List<String>> entry : flattenDb.entrySet()){
                        int index = -1;
                        if((index = entry.getValue().indexOf(path))>=0){
                            entry.getValue().remove(index);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return flattenDb;
    }

    public boolean mergeDB(String otherDBPath){
        boolean hasChanged= false;
        try {
            JSONObject myJSON = getJson();
            KeywordsHelper otherDBHelper = new KeywordsHelper(mContext, otherDBPath);
            JSONObject otherDBJson = otherDBHelper.getJson();
            for(int i = 0; i<otherDBJson.getJSONArray("data").length(); i++){
                JSONObject obj = otherDBJson.getJSONArray("data").getJSONObject(i);
                if(!obj.has("keyword"))
                    continue;
                String action = obj.getString("action");
                String path = obj.getString("path");
                String keyword = obj.getString("keyword");
                long time = obj.getLong("time");
                boolean isIn = false;
                int index = 0;
                for(int j = 0; j<myJSON.getJSONArray("data").length(); j++){
                    JSONObject myObj = myJSON.getJSONArray("data").getJSONObject(j);
                    if(!myObj.has("keyword"))
                        continue;
                    String myAction = myObj.getString("action");
                    String myPath = myObj.getString("path");
                    String myKeyword = myObj.getString("keyword");
                    long myTime = myObj.getLong("time");
                    if(keyword.equals(myKeyword) && myAction.equals(action) && myPath.equals(path) && myTime == time){
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return hasChanged;
    }
}
