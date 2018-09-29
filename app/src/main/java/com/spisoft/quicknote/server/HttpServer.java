package com.spisoft.quicknote.server;

import android.content.Context;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.spisoft.quicknote.databases.KeywordsHelper;
import com.spisoft.quicknote.utils.FileUtils;
import com.spisoft.quicknote.utils.ZipUtils;

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

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

public class HttpServer extends NanoHTTPD {

    private static final String TAG = "HttpServer";
    private final Context mContext;
    private final String extractedNotePath;

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

        if(path!=null){
            Log.d("pathdebug","path: "+path);

            if(path.startsWith("/api/")){
                String subpath = path.substring("/api/".length());
                switch (subpath){
                    case "note/open":
                        return openNote(parms.get("path").get(0));

                    case "keywordsdb":
                        return getKeywordDB();
                    case "note/saveText":
                        if(post.get("path")!=null && post.get("path").size()>=0 && post.get("html")!=null && post.get("html").size()>=0 && post.get("metadata")!=null && post.get("metadata").size()>=0)
                            return saveNote(post.get("path").get(0),post.get("html").get(0),post.get("metadata").get(0));


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

    private Response saveNote(String path, String html, String metadata) {
        List <String> except = new ArrayList<>();
        except.add(extractedNotePath+"reader.html");
        ZipUtils.zipFolder(new File(extractedNotePath), path,except);
        return null;
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
        Log.d(TAG, "opening note "+path);
        try {

            if(new File(path).exists()) {
                JSONObject object = new JSONObject();
                object.put("id","");
                ZipUtils.unzip(path, extractedNotePath);
                File f = new File(extractedNotePath, "index.html");
                if (f.exists()) {
                    String index = FileUtils.readFile(f.getAbsolutePath());
                    object.put("html",index);
                }
                f = new File(extractedNotePath, "metadata.json");
                if (f.exists()) {
                    String meta = FileUtils.readFile(f.getAbsolutePath());
                    object.put("metadata",new JSONObject(meta));
                }
                return  NanoHTTPD.newChunkedResponse(Response.Status.OK, "application/json",new ByteArrayInputStream(object.toString().getBytes()));

            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return NanoHTTPD.newFixedLengthResponse(Response.Status.NOT_FOUND,"","");
    }

    public String getUrl(String path){
        int port = getListeningPort();
        String url = "http://localhost:"+port+path;
        Log.d(TAG, url);
        return url;
    }
}