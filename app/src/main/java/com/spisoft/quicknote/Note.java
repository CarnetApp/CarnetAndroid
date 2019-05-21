package com.spisoft.quicknote;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by alexandre on 01/02/16.
 */
public class Note implements Serializable{
    public String path;
    public String title;
    public String shortText = "";
    public long file_lastmodification = -1;
    public static final String PAGE_INDEX_PATH = "page_index.json";
    public long lastModified = -1;
    public ArrayList<String> keywords;
    public Metadata mMetadata = new Metadata();
    public boolean isPinned = false;
    public ArrayList<String> previews;
    public ArrayList<String> medias;
    public boolean needsUpdateInfo = true;
    public boolean hasFound;
    public boolean isFake;

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
        String name = new File(path).getName();
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
        String name = new File(path).getName();
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

    public void hasFound(Boolean hasFound) {
        this.hasFound = hasFound;
    }

    public static class TodoList implements Serializable{
        public List<String> todo = new ArrayList<>();
        public List<String> done = new ArrayList<>();
        public String id = "";
        public static TodoList fromString(String string){
            TodoList todoList = new TodoList();
            try {
                JSONObject jsonObject = new JSONObject(string);
                return fromJSONObject(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return todoList;
        }

        public static TodoList fromJSONObject(JSONObject jsonObject){
            TodoList todoList = new TodoList();
            try {
                todoList.id = jsonObject.getString("id");
                JSONArray array =  jsonObject.getJSONArray("todo");
                for (int i = 0; i < array.length(); i++) {
                    todoList.todo.add(array.getString(i));
                }
                array =  jsonObject.getJSONArray("done");
                for (int i = 0; i < array.length(); i++) {
                    todoList.done.add(array.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return todoList;
        }

        public JSONObject toJsonObject(){
            JSONObject object = new JSONObject();
            try {
                object.put("id",id);
                JSONArray todo = new JSONArray();
                for(String item : this.todo){
                    todo.put(item);
                }
                object.put("todo",todo);

                JSONArray done = new JSONArray();
                for(String item : this.done){
                    done.put(item);
                }
                object.put("done",done);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return object;
        }

    }

    public static class Reminder implements Serializable{
        public List<String> days = new ArrayList<>();
        public int dayOfMonth = -1;
        public int month = -1;
        public int year = -1;
        public long time = -1;
        public String frequency = null;
        public String id = "";
        public static Reminder fromString(String string){
            Reminder reminder = new Reminder();
            try {
                JSONObject jsonObject = new JSONObject(string);
                return fromJSONObject(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return reminder;
        }

        public static Reminder fromJSONObject(JSONObject jsonObject){
            Reminder reminder = new Reminder();
            try {
                reminder.id = jsonObject.getString("id");
                if(jsonObject.has("days")) {
                    JSONArray array = jsonObject.getJSONArray("days");
                    for (int i = 0; i < array.length(); i++) {
                        reminder.days.add(array.getString(i));
                    }
                }
                reminder.frequency = jsonObject.getString("frequency");
                reminder.time = jsonObject.getLong("time");
                if(jsonObject.has("dayOfMonth"))
                    reminder.dayOfMonth = jsonObject.getInt("dayOfMonth");
                if(jsonObject.has("year"))
                    reminder.year = jsonObject.getInt("year");
                if(jsonObject.has("month"))
                    reminder.month = jsonObject.getInt("month");


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return reminder;
        }

        public JSONObject toJsonObject(){
            JSONObject object = new JSONObject();
            try {
                object.put("id",id);
                JSONArray days = new JSONArray();
                for(String item : this.days){
                    days.put(item);
                }
                object.put("days",days);
                object.put("time",time);
                object.put("dayOfMonth",dayOfMonth);
                object.put("year",year);
                object.put("month",month);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return object;
        }

    }

    public static class Metadata implements Serializable{
        public long creation_date = -1;
        public long custom_date = -1;
        public long last_modification_date = -1;
        public List<String> keywords = new ArrayList();
        public List<TodoList> todolists = new ArrayList();
        public List<Reminder> reminders = new ArrayList();
        public List<String> urls = new ArrayList<>();
        public int rating = -1;
        public String color = "none";

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Metadata metadata = (Metadata) o;

            if (creation_date != metadata.creation_date) return false;
            if (last_modification_date != metadata.last_modification_date) return false;
            if (rating != metadata.rating) return false;
            if (color != metadata.color) return false;
            return keywords != null ? keywords.equals(metadata.keywords) : metadata.keywords == null;
        }

        @Override
        public int hashCode() {
            int result = (int) (creation_date ^ (creation_date >>> 32));
            result = 31 * result + (int) (last_modification_date ^ (last_modification_date >>> 32));
            result = 31 * result + (keywords != null ? keywords.hashCode() : 0);
            result = 31 * result + rating;
            result = 31 * result + color.hashCode();
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
                if(jsonObject.has("custom_date"))
                    metadata.custom_date = jsonObject.getLong("custom_date");

                if(jsonObject.has("rating"))
                    metadata.rating = jsonObject.getInt("rating");
                if(jsonObject.has("color"))
                    metadata.color = jsonObject.getString("color");

                JSONArray array =  jsonObject.getJSONArray("keywords");
                for (int i = 0; i < array.length(); i++) {
                    metadata.keywords.add(array.getString(i));
                }
                if(jsonObject.has("todolists")) {
                    array = jsonObject.getJSONArray("todolists");
                    for (int i = 0; i < array.length(); i++) {
                        metadata.todolists.add(TodoList.fromJSONObject(array.getJSONObject(i)));
                    }
                }

                if(jsonObject.has("urls")){
                    JSONObject obj = jsonObject.getJSONObject("urls");
                    Iterator<String> iterator = obj.keys();
                    while(iterator.hasNext()){
                        metadata.urls.add(iterator.next());
                    }
                }
                if(jsonObject.has("reminders")){
                    JSONArray array1 = jsonObject.getJSONArray("reminders");
                    for(int i = 0; i < array1.length(); i++){
                        metadata.reminders.add(Reminder.fromJSONObject(array1.getJSONObject(i)));
                    }
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
                if(custom_date != -1)
                    object.put("custom_date",custom_date);

                object.put("rating",rating);
                object.put("color",color);
                JSONArray keywordsJson = new JSONArray();
                for(String keyword : keywords){
                    keywordsJson.put(keyword);
                }
                object.put("keywords",keywordsJson);
                JSONArray todolists = new JSONArray();
                for(TodoList todoList : this.todolists){
                    todolists.put(todoList.toJsonObject());
                }
                object.put("todolists",todolists);
                JSONObject urlsObj = new JSONObject();

                for(String url : urls){
                    urlsObj.put(url, new JSONObject());
                }
                object.put("urls", urlsObj);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return object;
        }
    }
}
