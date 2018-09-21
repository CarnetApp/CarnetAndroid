package com.spisoft.quicknote.editor;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.provider.OpenableColumns;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.spisoft.quicknote.FloatingFragment;
import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.R;
import com.spisoft.quicknote.databases.KeywordsHelper;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.databases.RecentHelper;
import com.spisoft.quicknote.databases.page.Page;
import com.spisoft.quicknote.editor.pages.PagesAdapter;
import com.spisoft.quicknote.server.NewHttpProxy;
import com.spisoft.quicknote.serviceactivities.CropWrapperActivity;
import com.spisoft.quicknote.utils.FileUtils;
import com.spisoft.quicknote.utils.ZipUtils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.spisoft.quicknote.MainActivity.ACTION_RELOAD_KEYWORDS;
import static com.spisoft.quicknote.browser.NoteListFragment.ACTION_RELOAD;


/**
 * Created by phoenamandre on 01/02/16.
 */
public class EditorView extends FrameLayout implements View.OnClickListener, CropWrapperActivity.CroperResultListener, FloatingFragment, PictureEditorFloatingFragment.OnEditEndListener, ZipUtils.WriterListener, PagesAdapter.OnPageSelectedListener {


    private static final String TAG = "EditorView";
    private static final int OPEN_MEDIA_REQUEST = 343;
    private WebView mWebView;
    private Note mNote;

    private HideListener mRenameListener;

    private LinearLayout mLinearLayout;
    private View mProgressLayout;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

        }

    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(NoteManager.ACTION_MOVE)) {
                if (mNote != null && mNote.path.equals(intent.getStringExtra(NoteManager.OLD_PATH))) {
                    mNote.setPath(intent.getStringExtra(NoteManager.NEW_PATH));
                    rename(intent.getStringExtra(NoteManager.OLD_PATH), mNote.path);
                }

            }
        }
    };

    private FakeFragmentManager mFragmentManager;

    private String mRootPath;
    private NewHttpProxy mServer2;
    private boolean mSetNoteOnLoad;
    public static EditorView sEditorView;
    private String mSelectFileCallback;

    private void rename(String stringExtra, String path) {
        mWebView.loadUrl("javascript:replace('" + StringEscapeUtils.escapeEcmaScript(stringExtra) + "','" + StringEscapeUtils.escapeEcmaScript(path) + "')");

    }

    private android.content.IntentFilter mFilter;

    private boolean mHasRequestedSave;
    public static String editedAbsolutePath = null;

    public EditorView(Context context) {
        super(context);
        init();
    }

    public EditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if(requestCode == OPEN_MEDIA_REQUEST && resultCode == Activity.RESULT_OK){
            Log.d(TAG, data.getDataString());

            Cursor cursor = null;
            String displayName = data.getData().getLastPathSegment();
            try {
                cursor = getContext().getContentResolver().query(data.getData(), null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
            Log.d(TAG, displayName);
            try {
                final File outF = new File(getContext().getExternalCacheDir(), displayName);
                InputStream in = getContext().getContentResolver().openInputStream(data.getData());
                OutputStream out = new FileOutputStream(outF);
                FileUtils.copy(in, out);
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        String retFunction = "FileOpener.selectFileResult('"+mSelectFileCallback+"','" + outF.getAbsolutePath() + "')";
                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT)
                            mWebView.evaluateJavascript(retFunction,null);
                        else
                            mWebView.loadUrl("javascript:"+retFunction);
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    public void createNewNote() {
        setNote(NoteManager.createNewNote(new File(mNote.path).getParent()));
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setOptionMenu(ViewGroup container) {
        LayoutInflater.from(getContext()).inflate(R.layout.editor_option_menu, container);

    }

    @Override
    public void onEditEnd(String id, String path, boolean reload) {

        mWebView.loadUrl("javascript:reloadImage('" + id + "', '" + StringEscapeUtils.escapeEcmaScript(path) + "')");
    }

    @Override
    public void onDelete(String id, String path) {
        Log.d("deletedebug", "delete " + id);
        mWebView.loadUrl("javascript:deleteImage('" + id + "')");
    }

    public Note getNote() {
        return mNote;
    }


    @Override
    public void onError() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (getContext() != null) ;
                Toast.makeText(getContext(), R.string.write_error, Toast.LENGTH_LONG).show();
            }
        });

    }

    public void reset() {
        mWebView.requestFocus();
    }

    @Override
    public void onPageSelected(Page page){
    }

    public interface HideListener {
        public void onHide(boolean hide);

        void onExit();
    }

    public void setHideListener(HideListener listener) {
        mRenameListener = listener;
    }

    private void init() {
        // Inflate the layout for this fragment
        sEditorView = this;
        mFilter = new IntentFilter();
        mFilter.addAction(NoteManager.ACTION_MOVE);

        mLinearLayout = new LinearLayout(getContext());
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.setLayoutTransition(new LayoutTransition());
        addView(mLinearLayout);
        mProgressLayout = LayoutInflater.from(getContext()).inflate(R.layout.progress_layout, null, true);
        addView(mProgressLayout);

        mWebView = new MyWebView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);

        //params.topMargin = getResources().getDimensionPixelSize(R.dimen.editor_vertical_margin);

        mLinearLayout.addView(mWebView, params);
        mWebView.requestFocus();
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        //mWebView.getSettings().setSupportZoom(false);
        mWebView.setWebViewClient(mClient);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.addJavascriptInterface(new WebViewJavaScriptInterface(getContext()), "app");
        mHasLoaded = false;
        mRootPath = getContext().getFilesDir().getAbsolutePath();
        try {
            mServer2 = new NewHttpProxy(getContext());
            mWebView.loadUrl(mServer2.getUrl("/tmp/reader.html"));

        } catch (IOException e) {
            e.printStackTrace();
        }
        //prepare Reader
        //extract

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }


    }



    public void loadNote(){
        Log.d(TAG, "loadNote");

        File dir = new File(mRootPath + "/tmp");
        List<String> except =new ArrayList();
        except.add(mRootPath + "/tmp/reader.html");
        FileUtils.deleteRecursive(dir, except);
        mWebView.loadUrl("javascript:loadPath('" + Uri.encode(mNote.path) + "')");

    }

    public void setNote(Note note) {
        mProgressLayout.setAlpha(1);
        try {
            getContext().registerReceiver(mBroadcastReceiver, mFilter);
        } catch (Exception e) {
        }
        mProgressLayout.setVisibility(VISIBLE);
        mNote = note;
        mSetNoteOnLoad = !mHasLoaded;
        if(mHasLoaded) {
            loadNote();
        }
        editedAbsolutePath = mNote.path;
    }

    public void onDestroy() {
        try {
            getContext().unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
        }
        mHasRequestedSave = true;
        editedAbsolutePath = null;
        getContext().sendBroadcast(new Intent(ACTION_RELOAD_KEYWORDS));
        /*Intent intent = new Intent(ACTION_RELOAD);
        List<Note> notes = new ArrayList<>();
        notes.add(mNote);
        intent.putExtra("notes",mNote);
        getContext().sendBroadcast(intent);*/
    }

    private void sendAction(String string) {
        mWebView.loadUrl("javascript:formatDoc('" + StringEscapeUtils.escapeEcmaScript(string) + "')");
    }

    private void sendAction(String string, String value) {


        mWebView.loadUrl("javascript:formatDoc('" + StringEscapeUtils.escapeEcmaScript(string) + "', '" + StringEscapeUtils.escapeEcmaScript(value) + "')");
    }


    @Override
    public void onClick(View view) {

    }


    public boolean mHasLoaded;

    public class WebViewJavaScriptInterface {

        private Context context;


        /*
         * Need a reference to the context in order to sent a post message
         */
        public WebViewJavaScriptInterface(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void unlink(final String path, final String callback) {

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    String pathNotFi = path;
                    if (!pathNotFi.startsWith("/"))
                        pathNotFi = mRootPath + "/" + pathNotFi;
                    Log.d(TAG, "deleting " + pathNotFi);
                    FileUtils.deleteRecursive(new File(pathNotFi));
                    return null;
                }

                protected void onPostExecute(Void result) {
                    mWebView.loadUrl("javascript:FSCompatibility.unlinkResult('" + callback + "')");

                }
            }.execute();
        }
        @JavascriptInterface
        public void readdir(String path, final String callback){
            if (!path.startsWith("/"))
                path = mRootPath + "/" + path;
            final JSONObject object = new JSONObject();
            JSONArray array = new JSONArray();
            final File file = new File(path);
            File[] files = file.listFiles();
            if(files!=null)
            for(File f : files){
                Log.d(TAG, "read dir ");
                Log.d(TAG, "read dir"+ f.getAbsolutePath());

                array.put(f.getAbsolutePath().substring(path.length()));
            }
            try {
                object.put("data",array);

                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "read dir"+ file.getAbsolutePath());
                        mWebView.loadUrl("javascript:FSCompatibility.resultReaddir('" + callback + "',false,'"
                                +StringEscapeUtils.escapeEcmaScript(object.toString())+"')");

                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        @JavascriptInterface
        public void addKeyword(String word, String path){
            KeywordsHelper.getInstance(getContext()).addKeyword(word, new Note(path));
        }

        @JavascriptInterface
        public void removeKeyword(String word, String path){
            KeywordsHelper.getInstance(getContext()).removeKeyword(word, new Note(path));
        }

        @JavascriptInterface
        public void selectFile(String callback) {
            mSelectFileCallback = callback;
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    ((Activity)getContext()).startActivityForResult(intent, OPEN_MEDIA_REQUEST);

                }
            });
        }
        @JavascriptInterface
        public void extractTo(String from, String to, final String callback){
            boolean error;
            if (!from.startsWith("/"))
                from = mRootPath + "/" + from;
            if (!to.startsWith("/"))
                to = mRootPath + "/" + to;
            try {
                ZipUtils.unzip(from, to);
                error = false;

            } catch (IOException e) {
                e.printStackTrace();
                error = true;
            }
            final boolean finalError = error;
            mWebView.post(new Runnable() {
                              @Override
                              public void run() {
                                  mWebView.loadUrl("javascript:NoteOpenerResultReceiver.extractResult('" + callback + "',"+ finalError +");");
                              }
                          }
            );
        }

        @JavascriptInterface
        public void readFile(final String callback, String path) {
            if (!path.startsWith("/")&& ! path.startsWith("content"))
                path = mRootPath + "/" + path;
            File f = new File(path);
            Log.d(TAG, "read " + path);

            InputStream in = null;
            try {
                int length = -1;
                if(!path.startsWith("content")) {
                    in = new FileInputStream(f);
                    length = (int)f.length();
                }
                else {
                    in = getContext().getContentResolver().openInputStream(Uri.parse("content://com.android.providers.media.documents/document/image%3A116742"));
                    length = in.available();
                    Log.d(TAG, "readdiiiing " + in.available());
                }
                final byte[] content = new byte[length];

                for (int off = 0, read;
                     (read = in.read(content, off, content.length - off)) > 0;
                     off += read){
                    Log.d(TAG,"readdiiiing");
                }
                    ;
                Log.d(TAG, "result " + Base64.encodeToString(content, 0));

                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        final String retFunction = "FSCompatibility.resultFileRead('" + callback + "',false,'" + StringEscapeUtils.escapeEcmaScript(Base64.encodeToString(content, 0)) + "');";
                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT)
                            mWebView.evaluateJavascript(retFunction,null);
                        else
                            mWebView.loadUrl("javascript:"+retFunction);

                    }
                });
            } catch (IOException e) {
                // Some error occured
                e.printStackTrace();
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {

                        mWebView.loadUrl("javascript:FSCompatibility.resultFileRead('" + callback + "',true,'')");

                    }
                });
            } finally {
                if (in != null)
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
            }

        }

        @JavascriptInterface
        public void onBackPressed() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mRenameListener.onExit();
                }
            });
        }

        @JavascriptInterface
        public void mkdirs(String path) {
            Log.d(TAG, path);
            if (!path.startsWith("/"))
                path = mRootPath + "/" + path;
            new File(path).mkdirs();
        }

        @JavascriptInterface
        public void hideProgress() {
            Log.d(TAG, "hideProgress");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mProgressLayout.animate().alpha(0).setDuration(500).start();
                }
            },1);
        }

        @JavascriptInterface
        public void zipDir(String dir, String out, final String callback) {
            if (!dir.startsWith("/"))
                dir = mRootPath + "/" + dir;
            if (!out.startsWith("/"))
                out = mRootPath + "/" + dir;
            Log.d(TAG, "zipping " + dir + " to " + out);

            final String finalDir = dir;
            final String finalOut = out;
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    ZipUtils.zipFolder(new File(finalDir), finalOut);

                    return null;
                }

                protected void onPostExecute(Void result) {
                    mWebView.loadUrl("javascript:ArchiverCompatibility.finalizeResult('" + callback + "')");

                }
            }.execute();
        }
        @JavascriptInterface
        public void getFlatenKeywordsDB(final String callback) {

            new AsyncTask<Void, Void, String>() {

                @Override
                protected String doInBackground(Void... voids) {
                    Map<String, List<String>> keywords = KeywordsHelper.getInstance(getContext()).getFlattenDB(-1);
                    JSONObject object = new JSONObject();
                    for(Map.Entry<String, List<String>> entry : keywords.entrySet()){
                        JSONArray keyArray = new JSONArray();
                        for(String path : entry.getValue()){
                            keyArray.put(path);
                        }
                        try {
                            object.put(entry.getKey(), keyArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    return object.toString();
                }

                protected void onPostExecute(String result) {
                    mWebView.loadUrl("javascript:KeywordDBManagerCompatibility.getFlatenDBResult('" + callback + "','"+StringEscapeUtils.escapeEcmaScript(result)+"')");

                }
            }.execute();
        }


        @JavascriptInterface
        public void writeFileSync(String path, String content, String encoding) {
            byte[] data = null;
            if (encoding.equals("base64")) {
                try {
                    data = Base64.decode(content, 0);
                } catch (java.lang.IllegalArgumentException e) {

                }
            } else try {
                data = content.getBytes(encoding);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                OutputStream stream = new FileOutputStream(mRootPath + "/" + path);
                stream.write(data);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void writeFile(final String path, final String content, final String callback, final String encoding) {

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    writeFileSync(path, content, encoding);
                    return null;
                }

                protected void onPostExecute(Void result) {
                    mWebView.loadUrl("javascript:FSCompatibility.writeFileResult('" + callback + "')");
                }
            }.execute();

        }



        @JavascriptInterface
        public void alert(final String path) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("alertdebug", "alerting " + path);
                }
            });
        }
    }

    WebViewClient mClient = new WebViewClient() {
        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            handler.cancel();
        }

        /*
        **  Manage if the url should be load or not, and get the result of the request
        **
        */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {


            return true;
        }


        /*
        **  Catch the error if an error occurs
        **
        */
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

        }


        /*
        **  Display a dialog when the page start
        **
        */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }


        /*
        **  Remove the dialog when the page finish loading
        **
        */
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mHasLoaded = true;
            if(mSetNoteOnLoad){
                loadNote();
                mSetNoteOnLoad = false;
            }

        }
    };

    private void onNoteAndPageReady() {
    }


}
