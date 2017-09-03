package com.spisoft.quicknote.browser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandre on 12/05/16.
 */
public class PasteBin {
    private static ArrayList<Object> sPastebin = new ArrayList<>();
    public synchronized static void addObject(Object object){
        sPastebin.add(object);
    }
    public synchronized static void addObjects(List<Object> objects){
        sPastebin.addAll(objects);
    }
    public synchronized static void clear(){
        sPastebin.clear();
    }

    public synchronized static ArrayList<Object> getPasteBin(){
        return new ArrayList<>(sPastebin);
    }
}
