package com.spisoft.quicknote.editor;

import android.Manifest;
import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
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
import com.spisoft.quicknote.R;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.databases.RecentHelper;
import com.spisoft.quicknote.editor.recorder.AudioRecorderJS;
import com.spisoft.quicknote.server.HttpServer;
import com.spisoft.quicknote.serviceactivities.CropWrapperActivity;
import com.spisoft.quicknote.utils.FileUtils;
import com.spisoft.quicknote.utils.ZipUtils;
import com.spisoft.sync.Log;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.spisoft.quicknote.MainActivity.ACTION_RELOAD_KEYWORDS;


/**
 * Created by phoenamandre on 01/02/16.
 */
public class EditorView extends FrameLayout implements CropWrapperActivity.CroperResultListener, FloatingFragment, ZipUtils.WriterListener{


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
    private List<Action> mActions;
    public static String sNextExtension;
    private AudioRecorderJS mAudioRecorder;

    public static class Action implements Serializable{
        public String type;
        public String value;
    }

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
        return mAudioRecorder.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if((requestCode == OPEN_MEDIA_REQUEST || requestCode == REQUEST_SELECT_FILE) && resultCode == Activity.RESULT_OK){
            Log.d(TAG, data.getDataString());

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                if (requestCode == REQUEST_SELECT_FILE)
                {
                    sNextExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(getContext().getContentResolver().getType(data.getData()));
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
        setNote(NoteManager.createNewNote(new File(mNote.path).getParent()), null);
    }


    private void doWebViewPrint(String ss) {
        WebView printWebView = new WebView(getContext());
        printWebView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                createWebPrintJob(view);
                super.onPageFinished(view, url);
            }
        });
        // Generate an HTML document on the fly:
        printWebView.loadDataWithBaseURL(null, ss, "text/html", "UTF-8", null);
    }

    // Thank you https://github.com/402d/TextToPrint/blob/master/app/src/main/java/ru/a402d/texttoprint/MainActivity.java#L327
    @TargetApi(19)
    public class PrintDocumentAdapterWrapper extends PrintDocumentAdapter {

        private final PrintDocumentAdapter delegate;
        PrintDocumentAdapterWrapper(PrintDocumentAdapter adapter){
            super();
            this.delegate = adapter;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
            delegate.onLayout(oldAttributes, newAttributes,  cancellationSignal, callback,  extras);
            Log.d("ANTSON","onLayout");
        }

        @Override
        public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
            delegate.onWrite( pages, destination,cancellationSignal,callback);
            Log.d("ANTSON","onWrite");
        }

        public void onFinish(){
            delegate.onFinish();
            Log.d("ANTSON","onFinish");
        }

    }
    @TargetApi(19)
    private void createWebPrintJob(WebView webView) {

        //create object of print manager in your device
        PrintManager printManager = (PrintManager) getContext().getSystemService(Context.PRINT_SERVICE);

        //create object of print adapter
        PrintDocumentAdapterWrapper printAdapter = new PrintDocumentAdapterWrapper(webView.createPrintDocumentAdapter());

        //provide name to your newly generated pdf file
        String jobName = "Text2Print";

        //open print dialog
        if (printManager != null) {
            printManager.print(jobName, printAdapter, new PrintAttributes.Builder().setMinMargins(new PrintAttributes.Margins(0, 0, 0, 0)).build());
        }
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

    public void askToExit() {
        mWebView.loadUrl("javascript:writer.askToExit()");
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
        mProgressLayout.setVisibility(GONE);
        addView(mProgressLayout);

        mWebView = new MyWebView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);

        //params.topMargin = getResources().getDimensionPixelSize(R.dimen.editor_vertical_margin);

        mLinearLayout.addView(mWebView, params);
        mWebView.requestFocus();
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(Log.isDebug);
        }

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
        Log.d("AudioRecorderJS", "try to start");
        Intent audioIntent = new Intent( getContext(), AudioRecorderJS.class);
        getContext().startService(audioIntent);
        getContext().bindService(audioIntent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mAudioRecorder = ((AudioRecorderJS.LocalBinder)service).getService();
                Log.d("AudioRecorderJS", "bound");

                mAudioRecorder.set((Activity)getContext(), mServer2, mWebView);
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.addJavascriptInterface(mAudioRecorder.getJs(), "AndroidRecorderJava");
                        mWebView.loadUrl(mServer2.getUrl(getUrl()));
                    }
                });

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mAudioRecorder = null;
            }
        }, Context.BIND_AUTO_CREATE);


        //prepare Reader
        //extract

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }


    }
    public String getUrl(){
        return "/tmp/reader.html";
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
        mWebView.loadUrl("javascript:loadPath('" + StringEscapeUtils.escapeEcmaScript(RecentHelper.getRelativePath(mNote.path, getContext())) + "')");

    }

    public void setNote(Note note, List<Action> actions) {
        mProgressLayout.setAlpha(1);
        mActions = actions;
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
        public void AudioRecorderStart(String channels, String bitrate, String sampleRate) {
            mAudioRecorder.start(channels, bitrate, sampleRate);
        }

        @JavascriptInterface
        public void AudioRecorderStop(String channels, String bitrate, String sampleRate) {
            mAudioRecorder.stop();
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
        public void print(final String data) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    doWebViewPrint(data);

                }
            });
        }

        @JavascriptInterface
        public void postMessage(String query, String message){
            if(query.equals("exit"))
                onBackPressed();
        }

        @JavascriptInterface
        public String getRequestToken(){
            return mServer2.generateID();
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
                    if (mActions != null) {
                        for(Action action : mActions){
                            mWebView.loadUrl("javascript:writer.handleAction('" + StringEscapeUtils.escapeEcmaScript(action.type) + "', '" + StringEscapeUtils.escapeEcmaScript(action.value) + "');");
                        }
                        mActions = null;
                    }
                }
            },1);
        }

        @JavascriptInterface
        public void openUrl(String url){
            if(!url.startsWith("http"))
                url = "http://"+url;
            final String finalUrl = url;
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl));
                    getContext().startActivity(browserIntent);
                }
            });
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
