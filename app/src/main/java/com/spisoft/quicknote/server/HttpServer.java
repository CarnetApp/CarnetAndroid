package com.spisoft.quicknote.server;

import android.content.Context;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.databases.KeywordsHelper;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.utils.FileUtils;
import com.spisoft.quicknote.utils.PictureUtils;
import com.spisoft.quicknote.utils.ZipUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

public class HttpServer extends NanoHTTPD {

    private static final String TAG = "HttpServer";
    private final Context mContext;
    private final String extractedNotePath;
    private String mCurrentNotePath;

    /**
     * logger to log to.
     */

    public static void main(String[] args) {
    }

    public HttpServer(Context ct) {
        super(0);
        ServerRunner.executeInstance(this);
        try {
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mContext = ct;
        extractedNotePath = mContext.getCacheDir().getAbsolutePath()+"/currentnote";
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String path = session.getUri();
        InputStream rinput = null;
        String fileMimeType = null;
        Map<String, List<String>> parms = session.getParameters();
        Response.IStatus status = Response.Status.OK;
        Log.d(TAG, "Path: "+path);
        Map<String, String> files = new HashMap<>();
        Map<String, List<String>> post = new HashMap<>();
        if(Method.POST.equals(method)) {
            try {
                session.parseBody(files);
                for (Map.Entry<String, String> entry : files.entrySet()) {
                    Log.d(TAG, entry.getKey() + ": " + entry.getValue());

                }
                post = session.getParameters();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ResponseException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "Post : "+post.toString());
        for (Map.Entry<String, List<String>> entry : post.entrySet()) {
            Log.d(TAG, entry.getKey());

        }
        Log.d(TAG, "Get : "+parms.toString());
        Log.d(TAG, "query params "+                session.getQueryParameterString());
        if(path!=null){
            if(path.contains("../") || path.equals(".."))
                return NanoHTTPD.newFixedLengthResponse(Response.Status.FORBIDDEN,"","");
            Log.d("pathdebug","path: "+path);

            if(path.startsWith("/api/")){
                String subpath = path.substring("/api/".length());
                if(Method.GET.equals(method)) {
                    switch (subpath) {
                        case "note/open":
                            return openNote(parms.get("path").get(0));
                        case "keywordsdb":
                            return getKeywordDB();
                        case "note/open/0/listMedia":
                            return listOpenMedia();
                    }
                    if(subpath.startsWith("note/open/0/getMedia/")){
                        return getMedia(subpath.substring("note/open/0/getMedia/".length()));
                    }
                }
                else if(Method.POST.equals(method)){
                    switch (subpath) {
                        case "notes/move":
                            String from = post.get("from").get(0);
                            String to = post.get("to").get(0);
                            if(from.startsWith("./"))
                                from = from.substring(2);
                            if(to.startsWith("./"))
                                to = to.substring(2);
                            return moveNote(from, to);
                        case "keywordsdb/action":
                            if(post.get("json") != null && post.get("json").size()>0)
                               return keywordActionDB(post.get("json").get(0));
                        case "note/saveText":
                            if (post.get("path") != null && post.get("path").size() >= 0 && post.get("html") != null && post.get("html").size() >= 0 && post.get("metadata") != null && post.get("metadata").size() >= 0)
                                return saveNote(post.get("path").get(0), post.get("html").get(0), post.get("metadata").get(0));
                        case "note/open/0/addMedia":
                            if (post.get("path").size() > 0 && post.get("media[]").size() > 0 && files.containsKey("media[]"))
                                return addMedia(post.get("path").get(0), files.get("media[]"), post.get("media[]").get(0));

                    }
                } else if(Method.DELETE.equals(method)){
                    switch (subpath) {
                        case "note/open/0/media":
                            return deleteMedia(parms.get("path").get(0), parms.get("media").get(0));

                    }

                }

            }
            else {
                fileMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(path));
                try {
                    rinput = new FileInputStream(mContext.getFilesDir().getAbsolutePath() + path);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    status = Response.Status.NOT_FOUND;
                }
            }

        }

        return NanoHTTPD.newChunkedResponse(status, fileMimeType, rinput);
    }

    private Response deleteMedia(String path, String media) {
        if(media.contains("../") || media.equals(".."))
            return NanoHTTPD.newFixedLengthResponse(Response.Status.FORBIDDEN,"","");
        if(!mCurrentNotePath.equals(path)){
            return NanoHTTPD.newFixedLengthResponse(Response.Status.FORBIDDEN,"","");
        }
        new File(extractedNotePath+"/data/"+media).delete();
        saveNote(path);
        return listOpenMedia();
    }

    public void setCurrentNotePath(String path){
        mCurrentNotePath = path;
    }

    private Response keywordActionDB(String jason) {
        try {
            JSONArray object = new JSONArray(jason);
            for (int i = 0; i < object.length(); i++){
                if(object.getJSONObject(i).getString("action").equals("add")){
                    KeywordsHelper.getInstance(mContext).addKeyword(object.getJSONObject(i).getString("keyword"), new Note(object.getJSONObject(i).getString("path")));
                } else if (object.getJSONObject(i).getString("action").equals("remove")){
                    KeywordsHelper.getInstance(mContext).removeKeyword(object.getJSONObject(i).getString("keyword"), new Note(object.getJSONObject(i).getString("path")));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getKeywordDB();
    }

    private Response addMedia(String path, String tmpPath, String name) {
        if(name.contains("../") || name.equals(".."))
            return NanoHTTPD.newFixedLengthResponse(Response.Status.FORBIDDEN,"","");
        if(!mCurrentNotePath.equals(path)){
            return NanoHTTPD.newFixedLengthResponse(Response.Status.FORBIDDEN,"","");
        }
        Log.d(TAG, "adding media");
        File in = new File(tmpPath);
        if(in.exists()){
            File newF = new File(extractedNotePath+"/data/"+name);
            newF.getParentFile().mkdirs();
            in.renameTo(newF);
            if(PictureUtils.isPicture(name)) {
                File preview = new File(newF.getParentFile(), "preview_" + name);
                try {
                    PictureUtils.resize(newF.getAbsolutePath(), preview.getAbsolutePath(), 200, 200);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            saveNote(path);
        }
        return listOpenMedia();
    }

    private Response getMedia(String name){
        try {
            return  NanoHTTPD.newChunkedResponse(Response.Status.OK,  MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(name)),new FileInputStream(new File(new File(extractedNotePath, "data"), name)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        };
        return NanoHTTPD.newFixedLengthResponse(Response.Status.NOT_FOUND, "", "not found");

    }



    private Response listOpenMedia() {
        JSONArray array = new JSONArray();
        File f = new File(extractedNotePath, "data");
        if(f.exists()) {
            for(File c : f.listFiles()){
                if(!c.getName().startsWith("preview_")){
                    array.put("/api/note/open/0/getMedia/"+c.getName());
                }
            }
        }
        return  NanoHTTPD.newChunkedResponse(Response.Status.OK, "application/json",new ByteArrayInputStream(array.toString().getBytes()));
    }

    private Response saveNote(String path, String html, String metadata) {
        if(!mCurrentNotePath.equals(path)){
            return NanoHTTPD.newFixedLengthResponse(Response.Status.FORBIDDEN,"","");
        }
        FileUtils.writeToFile(extractedNotePath+"/index.html", html);
        FileUtils.writeToFile(extractedNotePath+"/metadata.json", metadata);
        return saveNote(path);
    }


    private Response saveNote(String path) {
        path = new File(PreferenceHelper.getRootPath(mContext),path).getAbsolutePath();
        List <String> except = new ArrayList<>();
        except.add(extractedNotePath+"/reader.html");
        ZipUtils.zipFolder(new File(extractedNotePath), path,except);
        return NanoHTTPD.newFixedLengthResponse("Saved !");
    }

    private Response moveNote(String from, String to) {
        Log.d(TAG, "MoveNote "+from +" to "+to);
        if(!mCurrentNotePath.equals(from)){
            Log.d(TAG, "Forbidden");

            return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR,"","");
        }
        boolean success = NoteManager.moveNote(mContext, new Note(new File(PreferenceHelper.getRootPath(mContext),from).getAbsolutePath()),  new File(PreferenceHelper.getRootPath(mContext),to).getAbsolutePath()) != null;
        if(success)
            mCurrentNotePath = to;
        Log.d(TAG, "MoveNote "+success);

        return success?NanoHTTPD.newFixedLengthResponse("Saved !"):NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR,"","");
    }

    private Response getKeywordDB() {
        JSONObject object = null;
        try {
            object = KeywordsHelper.getInstance(mContext).getJson();
            return  NanoHTTPD.newChunkedResponse(Response.Status.OK, "application/json",new ByteArrayInputStream(object.toString().getBytes()));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return NanoHTTPD.newFixedLengthResponse(Response.Status.NOT_FOUND,"","");
    }


    private Response openNote(String path) {

        if(!mCurrentNotePath.equals(path)){
            return NanoHTTPD.newFixedLengthResponse(Response.Status.FORBIDDEN,"","");
        }
        path = new File(PreferenceHelper.getRootPath(mContext),path).getAbsolutePath();
        Log.d(TAG, "opening note "+path);
        try {
            File dir = new File(extractedNotePath);
            List<String> except =new ArrayList();
            except.add(extractedNotePath + "/reader.html");
            FileUtils.deleteRecursive(dir, except);
            JSONObject object = new JSONObject();
            object.put("id","0");
            if(new File(path).exists()) {

                ZipUtils.unzip(path, extractedNotePath);
                File f = new File(extractedNotePath, "index.html")
                        ;
                if (f.exists()) {
                    String index = FileUtils.readFile(f.getAbsolutePath());
                    object.put("html",index);
                }
                f = new File(extractedNotePath, "metadata.json");
                if (f.exists()) {
                    String meta = FileUtils.readFile(f.getAbsolutePath());
                    object.put("metadata",new JSONObject(meta));
                }

            } else object.put("error","not found");

            return  NanoHTTPD.newChunkedResponse(Response.Status.OK, "application/json",new ByteArrayInputStream(object.toString().getBytes()));



        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return NanoHTTPD.newFixedLengthResponse(Response.Status.OK,"","");
    }

    public String getUrl(String path){
        int port = getListeningPort();
        String url = "http://localhost:"+port+path;
        Log.d(TAG, url);
        return url;
    }
}