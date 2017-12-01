package com.spisoft.quicknote.editor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;
import com.spisoft.quicknote.FloatingFragment;
import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.R;
import com.spisoft.quicknote.browser.NoteListFragment;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.databases.RecentHelper;
import com.spisoft.quicknote.databases.page.Page;
import com.spisoft.quicknote.databases.page.PageManager;
import com.spisoft.quicknote.editor.pages.PageView;
import com.spisoft.quicknote.editor.pages.PagesAdapter;
import com.spisoft.quicknote.server.NewHttpProxy;
import com.spisoft.quicknote.server.ZipReaderAndHttpProxy;
import com.spisoft.quicknote.serviceactivities.CropWrapperActivity;
import com.spisoft.quicknote.utils.AmbilWarnaDialog;
import com.spisoft.quicknote.utils.FileUtils;
import com.spisoft.quicknote.utils.PictureUtils;
import com.spisoft.quicknote.utils.SpiDebugUtils;
import com.spisoft.quicknote.utils.ZipWriter;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;


/**
 * Created by phoenamandre on 01/02/16.
 */
public class EditorView extends FrameLayout implements View.OnClickListener, CropWrapperActivity.CroperResultListener, FloatingFragment, PictureEditorFloatingFragment.OnEditEndListener, ZipWriter.WriterListener, PagesAdapter.OnPageSelectedListener {


    private static final String TAG = "EditorView";
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

        //prepare Reader
        //extract

        mRootPath = getContext().getFilesDir().getAbsolutePath();
        File dir = new File(mRootPath + "/reader");
        if (dir.exists())
            dir.delete();
        dir.mkdirs();
        copyFileOrDir("reader");
        //copy reader to separate folder and change rootpath
        String reader = FileUtils.readFile(mRootPath + "/reader/reader/reader.html");
        FileUtils.writeToFile(mRootPath + "/tmp/reader.html", reader.replace("<!ROOTPATH>", "../reader/"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

    }


    public void copyFileOrDir(String path) {
        Log.d("assetdebug", "copy " + path);
        AssetManager assetManager = getContext().getAssets();
        String assets[] = null;
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path);
            } else {
                String fullPath = mRootPath + "/" + path;
                File dir = new File(fullPath);
                if (!dir.exists())
                    dir.mkdirs();
                for (int i = 0; i < assets.length; ++i) {
                    copyFileOrDir(path + "/" + assets[i]);
                }
            }
        } catch (IOException ex) {
            Log.e("tag", "I/O Exception", ex);
        }
    }

    private void copyFile(String filename) {
        AssetManager assetManager = getContext().getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            String newFileName = mRootPath + "/" + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }


    public void setNote(Note note) {
        try {
            getContext().registerReceiver(mBroadcastReceiver, mFilter);
        } catch (Exception e) {
        }
        mProgressLayout.setVisibility(VISIBLE);
        mNote = note;
        try {
            mServer2 = new NewHttpProxy(getContext());
            mWebView.loadUrl(mServer2.getUrl("/tmp/reader.html") + "?path=" + Uri.encode(mNote.path));

        } catch (IOException e) {
            e.printStackTrace();
        }
        editedAbsolutePath = mNote.path;
        RecentHelper.getInstance(getContext()).addNote(mNote);
    }

    public void onDestroy() {
        try {
            getContext().unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
        }
        mHasRequestedSave = true;
        mWebView.loadUrl("javascript:requestSave()");
        editedAbsolutePath = null;
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
        public void readFile(final String callback, String path) {
            if (!path.startsWith("/"))
                path = mRootPath + "/" + path;
            File f = new File(path);
            Log.d(TAG, "read " + path);

            final byte[] content = new byte[(int) f.length()];
            InputStream in = null;
            try {
                in = new FileInputStream(f);
                for (int off = 0, read;
                     (read = in.read(content, off, content.length - off)) > 0;
                     off += read)
                    ;
                Log.d(TAG, "result " + Base64.encodeToString(content, 0));

                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("javascript:FSCompatibility.resultFileRead('" + callback + "',false,'" + Base64.encodeToString(content, 0) + "')");

                    }
                });
            } catch (IOException e) {
                // Some error occured
                e.printStackTrace();
                Log.d(TAG, "result " + Base64.encodeToString(content, 0));

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
            },500);
        }

        @JavascriptInterface
        public void zipDir(String dir, String out) {
            if (!dir.startsWith("/"))
                dir = mRootPath + "/" + dir;
            if (!out.startsWith("/"))
                out = mRootPath + "/" + dir;
            Log.d(TAG, "zipping " + dir + " to " + out);
            ZipWriter.zipFolder(new File(dir), out);
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
            onNoteAndPageReady();
        }
    };

    private void onNoteAndPageReady() {
    }


}
