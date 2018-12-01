package com.spisoft.quicknote.editor;

import android.Manifest;
import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
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
import com.spisoft.quicknote.server.HttpServer;
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
public class EditorView extends FrameLayout implements CropWrapperActivity.CroperResultListener, FloatingFragment, ZipUtils.WriterListener, PagesAdapter.OnPageSelectedListener {


    private static final String TAG = "EditorView";
    private static final int OPEN_MEDIA_REQUEST = 343;
    private static final int REQUEST_SELECT_FILE = 344;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 345;
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
    private HttpServer mServer2;
    private boolean mSetNoteOnLoad;
    public static EditorView sEditorView;
    private String mSelectFileCallback;
    private ValueCallback mUploadMessage;
    private PermissionRequest myRequest;

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

    public boolean onRequestPermissionsResult(int requestCode,
                                              String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_RECORD_AUDIO: {
                Log.d("WebView", "PERMISSION FOR AUDIO");
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    myRequest.grant(myRequest.getResources());

                } else {

                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if((requestCode == OPEN_MEDIA_REQUEST || requestCode == REQUEST_SELECT_FILE) && resultCode == Activity.RESULT_OK){
            Log.d(TAG, data.getDataString());

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                if (requestCode == REQUEST_SELECT_FILE)
                {
                    if (mUploadMessage == null)
                        return;
                    mUploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                    mUploadMessage = null;
                }
            }
            else if (requestCode == OPEN_MEDIA_REQUEST)
            {
                if (null == mUploadMessage)
                    return;
                // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
                // Use RESULT_OK only if you're implementing WebView inside an Activity
                Uri result = data == null || resultCode != Activity.RESULT_OK ? null : data.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
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
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                myRequest = request;

                for(String permission : request.getResources()) {
                    switch(permission) {
                        case "android.webkit.resource.AUDIO_CAPTURE": {
                            askForPermission(request.getOrigin().toString(), Manifest.permission.RECORD_AUDIO, PERMISSIONS_REQUEST_RECORD_AUDIO);
                            break;
                        }
                    }
                }
            }
            // For 3.0+ Devices (Start)
            // onActivityResult attached before constructor
            protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                ((Activity)getContext()).startActivityForResult(Intent.createChooser(i, "File Browser"), OPEN_MEDIA_REQUEST);
            }


            // For Lollipop 5.0+ Devices
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(null);
                    mUploadMessage = null;
                }

                mUploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try {
                    ((Activity)getContext()).startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e) {
                    mUploadMessage = null;
                    Toast.makeText(((Activity)getContext()).getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }

            //For Android 4.1 only
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                ((Activity)getContext()).startActivityForResult(Intent.createChooser(intent, "File Browser"), OPEN_MEDIA_REQUEST);
            }

            protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                ((Activity)getContext()).startActivityForResult(Intent.createChooser(i, "File Chooser"), OPEN_MEDIA_REQUEST);
            }
        });
        mWebView.addJavascriptInterface(new WebViewJavaScriptInterface(getContext()), "app");
        mHasLoaded = false;
        mRootPath = getContext().getFilesDir().getAbsolutePath();
        mServer2 = new HttpServer(getContext());
        mWebView.loadUrl(mServer2.getUrl("/tmp/reader.html"));
        //prepare Reader
        //extract

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }


    }

    public void askForPermission(String origin, String permission, int requestCode) {
        Log.d("WebView", "inside askForPermission for" + origin + "with" + permission);

        if (ContextCompat.checkSelfPermission(getContext().getApplicationContext(),
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions((Activity) getContext(),
                    new String[]{permission},
                    requestCode);

        } else {
            if(Build.VERSION.SDK_INT>=21)
                myRequest.grant(myRequest.getResources());
        }
    }

    public void loadNote(){
        Log.d(TAG, "loadNote");

        File dir = new File(mRootPath + "/tmp");
        List<String> except =new ArrayList();
        except.add(mRootPath + "/tmp/reader.html");
        FileUtils.deleteRecursive(dir, except);
        mServer2.setCurrentNotePath(RecentHelper.getRelativePath(mNote.path, getContext()));
        mWebView.loadUrl("javascript:loadPath('" + Uri.encode(RecentHelper.getRelativePath(mNote.path, getContext())) + "')");

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
        public void paste() {
            final ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = clipboard.getPrimaryClip();
            ClipData.Item item1 = clipData.getItemAt(0);
            final String text = item1.getText().toString();
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:document.execCommand('insertHTML', false, '" + StringEscapeUtils.escapeEcmaScript(text) + "');", null);
                }});
        }


        @JavascriptInterface
        public void postMessage(String query, String message){
            if(query.equals("exit"))
                onBackPressed();
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
        public void onBackPressed() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mRenameListener.onExit();
                }
            });
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

}
