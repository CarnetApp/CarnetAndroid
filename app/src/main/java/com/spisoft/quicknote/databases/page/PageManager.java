package com.spisoft.quicknote.databases.page;

import com.spisoft.quicknote.Note;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandre on 17/10/16.
 */

public class PageManager {

    private final Note mNote;
    private final ArrayList<Page> mPageList;
    private final static String PAGE_INIT_NAME = "page";

    public PageManager(Note note){
        mNote = note;
        mPageList = new ArrayList<>();
    }

    public Page getPage(int i){
        return mPageList.get(i);
    }

    public List<Page> getPageList(){
        return mPageList;
    }

    public void addPage(Page page){
        mPageList.add(page);
    }

    public Page createBlankPage(){
        int i = 1;
        boolean shouldContinue = true;
        while(shouldContinue){
            shouldContinue = false;
            for (Page page : mPageList){
                if(page.relativePath.equals(PAGE_INIT_NAME+i+".html")){
                    shouldContinue=true;
                    i++;
                    break;
                }
            }
        }
        Page page = new Page(Page.TYPE_HTML,PAGE_INIT_NAME+i+".html",null);
        return page;
    }

    public String toJson(){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(JSONObject.quote("pages"));
        sb.append(":");
        sb.append("[");
        for(int i = 0; i < mPageList.size(); i++){
            sb.append(mPageList.get(i).toJson());
            if(i<mPageList.size()-1)
                sb.append(",");
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }



}
