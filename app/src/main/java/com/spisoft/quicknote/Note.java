package com.spisoft.quicknote;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandre on 01/02/16.
 */
public class Note implements Serializable{
    public String path;
    public String title;
    public String shortText = "";
    public static final String PAGE_INDEX_PATH = "page_index.json";
    public long lastModified = -1;
    public ArrayList<String> keywords;
    public Metadata mMetadata = new Metadata();
    public boolean isPinned = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Note note = (Note) o;

        return path.equals(note.path);

    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    public Note(String path){
        String name = Uri.parse(path).getLastPathSegment();
        if(name.endsWith(".sqd"))
            name = name.substring(0, name.length()-".sqd".length());
        this.path = path;
        this.title = name;
        this.keywords = null;
    }
    public Note(String path, String title){
        this.path = path;
        this.title = title;
    }


    public void setPath(String path) {
        String name = Uri.parse(path).getLastPathSegment();
        if(name.endsWith(".sqd"))
            name = name.substring(0, name.length()-".sqd".length());
        this.path = path;
        this.title = name;
    }

    public void setShortText(String shortText){
        this.shortText = shortText;
    }

    public void setMetaData(Metadata metadata) {
        mMetadata = metadata;
    }

    public static class Metadata implements Serializable{
        public long creation_date = -1;
        public long last_modification_date = -1;
        public List<String> keywords = new ArrayList();
        public int rating = -1;
        public static Metadata fromString(String string){
            Metadata metadata = new Metadata();
            try {
                JSONObject jsonObject = new JSONObject(string);
                metadata.creation_date = jsonObject.getLong("creation_date");
                metadata.last_modification_date = jsonObject.getLong("last_modification_date");
                if(jsonObject.has("rating"))
                    metadata.rating = jsonObject.getInt("rating");
                JSONArray array =  jsonObject.getJSONArray("keywords");
                for (int i = 0; i < array.length(); i++) {
                    metadata.keywords.add(array.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return metadata;
        }
    }
}
