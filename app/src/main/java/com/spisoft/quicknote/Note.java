package com.spisoft.quicknote;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by alexandre on 01/02/16.
 */
public class Note implements Serializable{
    public String path;
    public String title;
    public static final String PAGE_INDEX_PATH = "page_index.json";
    public long lastModified = -1;
    public ArrayList<String> keywords;

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
}
