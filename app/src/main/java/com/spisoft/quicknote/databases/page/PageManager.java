package com.spisoft.quicknote.databases.page;

import android.util.JsonReader;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.server.ZipReaderAndHttpProxy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    public void fillPageList(ZipReaderAndHttpProxy mServer) {
        BufferedReader br = null;

        StringBuilder sb = new StringBuilder();
        try {
            InputStream stream = mServer.getZipInputStream(mServer.getZipEntry(mNote.PAGE_INDEX_PATH));

            if(stream!=null) {
                br = new BufferedReader(new InputStreamReader(stream));
                JsonReader jsonReader = new JsonReader(br);
                String line = br.readLine();
                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
                JSONObject jsonObject = new JSONObject(sb.toString());
                JSONArray array = jsonObject.getJSONArray("pages");
                for(int i = 0; i <array.length();i++){
                    Page page = Page.fromJsonObject(array.getJSONObject(i));
                    mPageList.add(page);
                }
            }

        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if(br!=null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        //adding default page
        if(mPageList.isEmpty()){
            mPageList.add(new Page(Page.TYPE_HTML, "index.html", null));
        }
    }


}
