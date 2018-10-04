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
    public ArrayList<String> previews;
    public ArrayList<String> medias;
    public boolean needsUpdateInfo = true;

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
        this.previews = new ArrayList<>();
        this.medias = new ArrayList<>();
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Metadata metadata = (Metadata) o;

            if (creation_date != metadata.creation_date) return false;
            if (last_modification_date != metadata.last_modification_date) return false;
            if (rating != metadata.rating) return false;
            return keywords != null ? keywords.equals(metadata.keywords) : metadata.keywords == null;
        }

        @Override
        public int hashCode() {
            int result = (int) (creation_date ^ (creation_date >>> 32));
            result = 31 * result + (int) (last_modification_date ^ (last_modification_date >>> 32));
            result = 31 * result + (keywords != null ? keywords.hashCode() : 0);
            result = 31 * result + rating;
            return result;
        }

        public static Metadata fromString(String string){
            Metadata metadata = new Metadata();
            try {
                JSONObject jsonObject = new JSONObject(string);
                return fromJSONObject(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return metadata;
        }

        public static Metadata fromJSONObject(JSONObject jsonObject){
            Metadata metadata = new Metadata();
            try {
                try { // sometimes creation date is on a bad format
                    metadata.creation_date = jsonObject.getLong("creation_date");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

        public JSONObject toJsonObject(){
            JSONObject object = new JSONObject();
            try {
                object.put("creation_date",creation_date);
                object.put("last_modification_date",last_modification_date);
                object.put("rating",rating);
                JSONArray keywordsJson = new JSONArray();
                for(String keyword : keywords){
                    keywordsJson.put(keyword);
                }
                object.put("keywords",keywordsJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return object;
        }
    }
}
