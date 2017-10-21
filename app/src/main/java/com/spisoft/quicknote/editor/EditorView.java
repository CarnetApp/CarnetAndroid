package com.spisoft.quicknote.editor;

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
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
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
import com.spisoft.quicknote.noise.NoiseService;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;


/**
 * Created by phoenamandre on 01/02/16.
 */
public class EditorView extends FrameLayout implements View.OnClickListener, CropWrapperActivity.CroperResultListener, FloatingFragment, PictureEditorFloatingFragment.OnEditEndListener, ZipWriter.WriterListener, PagesAdapter.OnPageSelectedListener {


    private static final String TAG = "EditorView";
    private View mRoot;
    private WebView mWebView;
    private Note mNote;
    private String mNoteString;
    private View mToolBar;
    private View mClearAllButton;
    private View mUndoButton;
    private View mRedoButton;
    private View mUnformatButton;
    private View mBoldButton;
    private View mItalicButton;
    private View mUnderlineButton;
    private View mAlignLeftButton;
    private View mAlignCenterButton;
    private View mAlignRightButton;
    private View mNumbersButton;
    private HideListener mRenameListener;
    private View mRenameBar;
    private View mRenameOKButton;
    private EditText mRenameED;
    private View mCopyButton;
    private View mPasteButton;
    private View mImageButton;
    private LinearLayout mLinearLayout;
    private View mProgressLayout;

    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            write((String) msg.obj);
        }

    };
    private View mTextZoneButton;
    private View mEditBar;
    private View mColorButton;
    private FrameLayout mDialogLayout;
    private View mHilightButton;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(NoteManager.ACTION_MOVE)){
                if(mNote!=null&&mNote.path.equals(intent.getStringExtra(NoteManager.OLD_PATH))){
                    mNote.setPath(intent.getStringExtra(NoteManager.NEW_PATH));
                    rename(intent.getStringExtra(NoteManager.OLD_PATH), mNote.path);
                }

            }
        }
    };
    private View mIncreaseFontSizeButton;
    private View mDecreaseFontSizeButton;
    private View mAlignJustifyButton;
    private View mStatisticsButton;
    private ZipReaderAndHttpProxy mServer;
    private boolean mHasSet;
    private FakeFragmentManager mFragmentManager;
    private PageManager mPageManager;
    private Page mPage;
    private int mCurrentPage;
    private boolean mWasAddingPage;
    private PageView mPageView;
    private ArrayList<View> mToolBars;
    private View mPagebarButton;
    private View mPageBar;
    private View mAddPageButton;

    private final static int EDITBAR_INT = 0;
    private final static int TOOLBAR_INT = 0;
    private final static int RENAMEBAR_INT = 0;
    private final static int PAGEBAR_INT = 0;
    private String mRootPath;
    private NewHttpProxy mServer2;

    private void rename(String stringExtra, String path) {
        mWebView.loadUrl("javascript:replace('"+StringEscapeUtils.escapeEcmaScript(stringExtra)+"','"+StringEscapeUtils.escapeEcmaScript(path)+"')");

    }

    private android.content.IntentFilter mFilter;
    private String mModifyId;
    private View mNewNoteButton;
    private View mFormatButton
            ;
    private View mEditButton;
    private View mRenameButton;
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
    private SecureRandom random = new SecureRandom();

    public String nextSessionId() {
        return new BigInteger(8, random).toString(12);
    }

    public String getDataDir(){
        return mNote.path+"/"+ "data";
    }
    public String getRelativeDataDir(){
        return "data";
    }


    private void takeScreenshot() {
        Process process;
        try {
            process = Runtime.getRuntime().exec("input keyevent 120");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!= Activity.RESULT_OK){
            if(mRenameListener!=null)
                mRenameListener.onHide(false);
            return;
        }

        if(requestCode == Crop.REQUEST_PICK){
            Uri result = Crop.getOutput(data);

            if(data.getData()!=null){
                File out = new File(getContext().getFilesDir(),nextSessionId()+".jpg");
                out.getParentFile().mkdirs();
                requestCrop(data.getData(), Uri.fromFile(out));



            }
        }
        else if(requestCode == Crop.REQUEST_CROP){
            if(data==null)
                return;
            Uri result = Crop.getOutput(data);
            if(result!=null){
                String path = "data/"+result.getLastPathSegment();
                if(ZipWriter.addEntry(getContext(),mNote, path, Uri.parse(result.getPath()))) {
                    if(!mWasAddingPage) {
                        if (mModifyId == null)
                            mWebView.loadUrl("javascript:insertFloatingElement('" + StringEscapeUtils.escapeEcmaScript("<img id=" + result.getLastPathSegment() + " src=\"" + path + "\" class=\"quicknote-img\"/>") + "')");
                        else {
                            mWebView.loadUrl("javascript:reloadImage('" + mModifyId + "', '" + StringEscapeUtils.escapeEcmaScript(path) + "')");
                        }

                    }
                    else {
                        File thumbnail = new File(getContext().getFilesDir(),nextSessionId()+".jpg");
                        thumbnail.getParentFile().mkdirs();
                        try {
                            PictureUtils.resize(result.getPath(), thumbnail.getAbsolutePath(), 400,700);
                            String thumbPath = "data/"+thumbnail.getName();
                            if(ZipWriter.addEntry(getContext(),mNote, thumbPath, Uri.parse(thumbnail.getAbsolutePath()))) {
                                Page page = new Page(Page.TYPE_IMG, result.getLastPathSegment() + ".html", thumbPath);
                                addPage(page, path);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    mWasAddingPage = false;
                    new File(result.toString()).delete();
                }
                //sendAction("insertHTML", );


            }
                if(mRenameListener!=null)
                    mRenameListener.onHide(false);

        }
        mModifyId = null;
    }

    private void addBlankPage(){
        addPage(mPageManager.createBlankPage(), null);
    }

    private void addPage(Page page, String path){
        //basically
        mPageManager.addPage(page);
        ZipWriter.savePageManager(getContext(), mNote, mPageManager, this);
        loadPage(mPageManager.getPageList().size()-1);
        if(path!=null) {
            mWebView.loadUrl("javascript:insertFloatingElement('" + StringEscapeUtils.escapeEcmaScript("<img id=" + Uri.parse(path).getLastPathSegment() + " src=\"" + path + "\" class=\"quicknote-img\"/>") + "')");
            mWebView.loadUrl("javascript:requestSave()");
        }
        mPageView.notifyDatasetChanged();
    }

    private void requestCrop(Uri data, Uri uri) {
        CropWrapperActivity.startCroper(getContext(), this, data, uri);
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
        LayoutInflater.from(getContext()).inflate(R.layout.editor_option_menu,container);
        mNewNoteButton = container.findViewById(R.id.new_note_button);
        mNewNoteButton.setOnClickListener(this);
        mFormatButton = container.findViewById(R.id.format_button);
        mFormatButton.setOnClickListener(this);
        mFormatButton.setActivated(true);
        mEditButton = container.findViewById(R.id.edit_button);
        mEditButton.setOnClickListener(this);
        mRenameButton = container.findViewById(R.id.rename_button);
        mRenameButton.setOnClickListener(this);
        mPagebarButton = container.findViewById(R.id.pagebar_button);
        mPagebarButton.setOnClickListener(this);
    }

    @Override
    public void onEditEnd(String id, String path, boolean reload) {

        mWebView.loadUrl("javascript:reloadImage('" + id + "', '" + StringEscapeUtils.escapeEcmaScript(path) + "')");
    }

    @Override
    public void onDelete(String id, String path) {
        Log.d("deletedebug","delete "+id);
        mWebView.loadUrl("javascript:deleteImage('" + id + "')");
    }

    public Note getNote() {
        return mNote;
    }

    public void addNotReadyPic(String mScreenshotPath) {
        Log.d("screendebug",mScreenshotPath);
        File out = new File(getDataDir(),nextSessionId()+".jpg");
        out.getParentFile().mkdirs();
        requestCrop(Uri.parse(mScreenshotPath), Uri.fromFile(out));
    }

    @Override
    public void onError() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(getContext()!=null);
                    Toast.makeText(getContext(), R.string.write_error, Toast.LENGTH_LONG).show();
            }
        });

    }

    public void reset() {
        displayFormatToolbar();
        mWebView.requestFocus();
    }

    @Override
    public void onPageSelected(Page page) {
        loadPage(mPageManager.getPageList().indexOf(page));
    }

    public interface HideListener {
        public void onHide(boolean hide);

        void onExit();
    }

    public void setHideListener(HideListener listener){
        mRenameListener = listener;
    }

    private void init() {
        // Inflate the layout for this fragment
        try {
            mServer = new ZipReaderAndHttpProxy(getContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mToolBars = new ArrayList<View>();
        mFilter = new IntentFilter();
        mFilter.addAction(NoteManager.ACTION_MOVE);

        mLinearLayout = new LinearLayout(getContext());
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.setLayoutTransition(new LayoutTransition());
        addView(mLinearLayout);
        mProgressLayout = LayoutInflater.from(getContext()).inflate(R.layout.progress_layout, null, true);
        addView(mProgressLayout);
        mDialogLayout = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.dialog_layout, null, true);
        addView(mDialogLayout);
        mDialogLayout.setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(R.layout.rename_layout, mLinearLayout, true);
        mRenameBar = findViewById(R.id.rename_bar);
        LayoutInflater.from(getContext()).inflate(R.layout.editor_toolbar, mLinearLayout, true);

        LayoutInflater.from(getContext()).inflate(R.layout.page_toolbar, mLinearLayout, true);
        mPageView = (PageView) findViewById(R.id.page_view);
        mPageView.setOnPageSelectedListener(this);
        mAddPageButton = findViewById(R.id.add_page_button);
        mPageBar = findViewById(R.id.page_bar);
        mToolBars.add(mPageBar);
        mToolBar  = findViewById(R.id.toolbar);
        mToolBars.add(mToolBar);
        LayoutInflater.from(getContext()).inflate(R.layout.editor_editbar, mLinearLayout, true);
        mEditBar  = findViewById(R.id.editbar);
        mToolBars.add(mEditBar);
        mRenameED = (EditText)mRenameBar.findViewById(R.id.name_ed);
        mRenameOKButton = mRenameBar.findViewById(R.id.button_rename_ok);
        mRenameOKButton.setOnClickListener(this);
        mRenameBar.setVisibility(GONE);
        mToolBars.add(mRenameBar);

        mClearAllButton = findViewById(R.id.clear_all);
        mUndoButton = findViewById(R.id.undo);
        mRedoButton = findViewById(R.id.redo);
        mCopyButton = findViewById(R.id.copy);
        mPasteButton = findViewById(R.id.paste);
        mUnformatButton = findViewById(R.id.unformat);
        mBoldButton = findViewById(R.id.bold);
        mItalicButton = findViewById(R.id.italic);
        mUnderlineButton = findViewById(R.id.underline);
        mAlignLeftButton = findViewById(R.id.align_left);
        mAlignCenterButton = findViewById(R.id.align_center);
        mAlignRightButton = findViewById(R.id.align_right);
        mAlignJustifyButton = findViewById(R.id.align_justify);
        mIncreaseFontSizeButton = findViewById(R.id.increase_font);
        mDecreaseFontSizeButton = findViewById(R.id.decrease_font);
        mNumbersButton = findViewById(R.id.numbers);
        mImageButton = findViewById(R.id.image);
        mColorButton = findViewById(R.id.color);
        mHilightButton = findViewById(R.id.hilight);
        mTextZoneButton= findViewById(R.id.text_zone);
        mStatisticsButton= findViewById(R.id.statistics_button);


        mAddPageButton.setOnClickListener(this);
        mHilightButton.setOnClickListener(this);
        mColorButton.setOnClickListener(this);
        mClearAllButton.setOnClickListener(this);
        mCopyButton.setOnClickListener(this);
        mPasteButton.setOnClickListener(this);
        mUndoButton.setOnClickListener(this);
        mRedoButton.setOnClickListener(this);
        mUnformatButton.setOnClickListener(this);
        mBoldButton.setOnClickListener(this);
        mItalicButton.setOnClickListener(this);
        mUnderlineButton.setOnClickListener(this);
        mAlignLeftButton.setOnClickListener(this);
        mAlignRightButton.setOnClickListener(this);
        mAlignCenterButton.setOnClickListener(this);
        mAlignJustifyButton.setOnClickListener(this);
        mNumbersButton.setOnClickListener(this);
        mImageButton.setOnClickListener(this);
        mTextZoneButton.setOnClickListener(this);
        mIncreaseFontSizeButton.setOnClickListener(this);
        mDecreaseFontSizeButton.setOnClickListener(this);
        mStatisticsButton.setOnClickListener(this);
    /*


        <ImageButton
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/puce" />
        <ImageButton
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/quote" />
        <ImageButton
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/tab_left" />
        <ImageButton
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/tab_right" />*/


        mWebView =new MyWebView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);

        //params.topMargin = getResources().getDimensionPixelSize(R.dimen.editor_vertical_margin);

        mLinearLayout.addView(mWebView, params);

        displayFormatToolbar();
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
        mRootPath = NoteManager.getDontTouchFolder(getContext());
        File dir = new File(mRootPath + "/reader");
        if(dir.exists())
            dir.delete();
        dir.mkdirs();
        copyFileOrDir("reader");
        //copy reader to separate folder and change rootpath
        String reader = FileUtils.readFile(mRootPath+"/reader/reader/reader.html");
        FileUtils.writeToFile(NoteManager.getDontTouchFolder(getContext())+"/tmp/reader.html", reader.replace("<!ROOTPATH>","../reader/"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

    }


    public void copyFileOrDir(String path) {
        Log.d("assetdebug", "copy "+path);
        AssetManager assetManager = getContext().getAssets();
        String assets[] = null;
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path);
            } else {
                String fullPath =mRootPath + "/" + path;
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
            String newFileName = mRootPath + "/" +filename;
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


    public void loadPage(int page){
        try {
            mProgressLayout.setVisibility(VISIBLE);
            mCurrentPage = page;
            mPage = mPageManager.getPage(page);
            mNoteString= read(mPage);
            if(mHasLoaded&&!mHasSet){
                onNoteAndPageReady();
            }

        } catch (IOException e) {
            Toast.makeText(getContext(), R.string.error_reading, Toast.LENGTH_LONG).show();
            Log.d(TAG, "reading error: IOException");
            Log.d("TestDebug","read error");

            if(mRenameListener!=null)
                mRenameListener.onExit();
            return;
        }
    }

    public void setNote(Note note){
        try{
            getContext().registerReceiver(mBroadcastReceiver, mFilter);
        }catch(Exception e){}
        mServer.setUri(note.path);
        mProgressLayout.setVisibility(VISIBLE);
        mNote = note;
        try {
            mServer2 = new NewHttpProxy(getContext());
            mWebView.loadUrl(mServer2.getUrl("/tmp/reader.html")+"?path="+Uri.encode(mNote.path));

        } catch (IOException e) {
            e.printStackTrace();
        }
        mPageManager = new PageManager(note);
        mPageManager.fillPageList(mServer);
        mPageView.setPageManager(mPageManager);
        mHasSet=false;
        editedAbsolutePath = mNote.path;
        RecentHelper.getInstance(getContext()).addNote(mNote);
        loadPage(0);
        /*mPageManager.addPage(new Page(Page.TYPE_HTML, "page2.html"));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadPage(1);
            }
        },7000);
        ZipWriter.savePageManager(getContext(), mNote, mPageManager,this);
*/
    }
    public void onDestroy(){
        try{
            getContext().unregisterReceiver(mBroadcastReceiver);
        }catch(Exception e){}
        mHasRequestedSave = true;
        mWebView.loadUrl("javascript:requestSave()");
        editedAbsolutePath = null;
    }

    private String read(Page page) throws IOException {
        mHasSet= false;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            InputStream stream = mServer.getZipInputStream(mServer.getZipEntry(page.relativePath));
            if(stream!=null) {
                br = new BufferedReader(new InputStreamReader(stream));
                String line = br.readLine();
                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
            }

        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }  finally {
            if(br!=null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        String ret = sb.toString();
        ret = NoteManager.update(ret);

        return ret;
    }

    private void write(String s) {
        if(s.contains("donotsave345oL")||s.isEmpty())
            return;
        s = s.replace("<div></div>","");
        s = s.replace("<br>","<br>\n");
        s = s.replace("<div><br></div>","<br>\n");

        ZipWriter.changeText(getContext(),mNote, mPage.relativePath, s, this);


    }

    private void sendAction(String string){
        mWebView.loadUrl("javascript:formatDoc('" + StringEscapeUtils.escapeEcmaScript(string) + "')");
    }

    private void sendAction(String string, String value){


        mWebView.loadUrl("javascript:formatDoc('" + StringEscapeUtils.escapeEcmaScript(string) + "', '" + StringEscapeUtils.escapeEcmaScript(value) + "')");
    }

    public void displayNameED(){
        mRenameED.setText(mNote.title);
        hideToolbarsExcept(mRenameBar);
    }

    private void hideToolbarsExcept(View toShow) {

        for(View v : mToolBars){
            if(v == toShow)
                v.setVisibility(GONE);
            else
                v.setVisibility(GONE);
        }
    }

    public void displayFormatToolbar(){
        hideToolbarsExcept(mToolBar);
    }
    public void displayEditToolbar(){
        hideToolbarsExcept(mEditBar);
    }
    @Override
    public void onClick(View view) {
        if(mAddPageButton==view){
            PopupMenu menu = new PopupMenu(getContext(),view);
            final MenuItem menuItemEmpty = menu.getMenu().add("Add empty page");
            MenuItem menuItemFiles = menu.getMenu().add("Add from files");
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(item == menuItemEmpty)
                        addBlankPage();
                    else{
                        mWasAddingPage = true;
                        CropWrapperActivity.startPicker(getContext(), EditorView.this);
                    }

                    return true;
                }
            });
            menu.show();
        }
        else if(view ==mClearAllButton){
            getContext().startService(new Intent(getContext(), NoiseService.class));
        }
        else if (view ==  mFormatButton){
            displayFormatToolbar();
            mFormatButton.setActivated(true);
        }else if (view ==  mEditButton){
            displayEditToolbar();
            mEditButton.setActivated(true);
        }else if (view ==  mRenameButton){
            displayNameED();
        }
        else if (view ==  mPagebarButton){
            displayPagebar();
        }
        else if (view == mNewNoteButton){
            createNewNote();

        }
        else if(view ==mUndoButton){
            sendAction("undo");
        }else if(view ==mDecreaseFontSizeButton){
            mWebView.loadUrl("javascript:decreaseFontSize();");

        }else if(view ==mIncreaseFontSizeButton){
            mWebView.loadUrl("javascript:increaseFontSize();");
        }else if(view ==mRedoButton){
            sendAction("redo");
        }else if(view ==mUnformatButton){
            sendAction("removeFormat");
        }else if(view ==mBoldButton){
            sendAction("bold");
        }else if(view ==mItalicButton){
            sendAction("italic");
        }else if(view ==mUnderlineButton){
            sendAction("underline");
        }else if(view ==mPasteButton){
            ClipboardManager clipboard = (ClipboardManager)
                    getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            sendAction("insertHTML", clipboard.getPrimaryClip().getItemAt(0).getText().toString());
        }else if(view ==mCopyButton){
            ClipboardManager clipboard = (ClipboardManager)
                    getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            sendAction("copy");
        }else if(view ==mAlignLeftButton){
            sendAction("justifyleft");
        }else if(view ==mAlignCenterButton){
            sendAction("justifycenter");
        } else if (view == mTextZoneButton) {
            mWebView.loadUrl("javascript:insertFloatingElement('" + StringEscapeUtils.escapeEcmaScript("<div class=\"quicknote-txtzone\" contenteditable=\"true\">edit me</div>") + "')");

        }
        else if(view ==mImageButton){
            if(mRenameListener!=null)
                mRenameListener.onHide(true);
            mWasAddingPage = false;
            CropWrapperActivity.startPicker(getContext(), this);
        }else if(view ==mAlignRightButton){
            sendAction("justifyright");
        }else if(view ==mNumbersButton){
            sendAction("insertorderedlist");
        }else if(view ==mAlignJustifyButton){
            sendAction("justifyFull");
        }
        else if(view ==mHilightButton){
            displayColorPickerDialog(true);
        }
        else if(view ==mColorButton){
            displayColorPickerDialog(false);
            takeScreenshot();
        }
        else if (view == mRenameOKButton){
            mRenameBar.setVisibility(GONE);
            String oldPath = mNote.path;
            String newPath = "";
            if((newPath =NoteManager.renameNote(getContext(),mNote, mRenameED.getText().toString()+".sqd"))!=null) {

                mNote.path = newPath;
                rename(oldPath, newPath);
            }
            getContext().sendBroadcast(new Intent(NoteListFragment.ACTION_RELOAD));
            Intent intent = new Intent(NoteManager.ACTION_MOVE);
            intent.putExtra(NoteManager.OLD_PATH, oldPath);
            intent.putExtra(NoteManager.NEW_PATH, mNote.path);
            getContext().sendBroadcast(intent);

        }
        else if(mStatisticsButton==view){
            mWebView.loadUrl("javascript:displayCountDialog()");

        }
    }

    private void displayPagebar() {
        hideToolbarsExcept(mPageBar);
    }

    public void displayColorPickerDialog(final boolean isBackground){
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(getContext(), ContextCompat.getColor(getContext(),android.R.color.black), new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                if(!isBackground)
                    sendAction("forecolor",""+ "#" + Integer.toHexString(color).substring(2) );
                else
                    sendAction("hilitecolor", "" + "#" + Integer.toHexString(color).substring(2));
                hideDialog();
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                hideDialog();
            }
        });

        setDialogView(dialog.getView());
    }



    private void hideDialog() {
        mDialogLayout.setVisibility(GONE);
    }

    private void setDialogView(View view) {
        mDialogLayout.removeAllViews();
        mDialogLayout.addView(view);
        mDialogLayout.setVisibility(VISIBLE);
        mDialogLayout.requestLayout();
        view.requestFocus();
    }

    public void setFakeFragmentManager(FakeFragmentManager fragManager){
        mFragmentManager = fragManager;
    }
    public boolean mHasLoaded;
    private long mLastRecord;
    public class WebViewJavaScriptInterface{

        private Context context;


        /*
         * Need a reference to the context in order to sent a post message
         */
        public WebViewJavaScriptInterface(Context context){
            this.context = context;
        }


        @JavascriptInterface
        public void onTextChange(String num) {

            if (System.currentTimeMillis() - mLastRecord >= 2000||mHasRequestedSave) {
                mLastRecord = System.currentTimeMillis();
                write(num);
            } else {
                mHandler.removeMessages(0);
                Message msg = mHandler.obtainMessage(0);
                msg.obj = num;
                mHandler.sendMessageDelayed(msg, 2000);
            }


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
                              @JavascriptInterface
                              public void editImage(final String path, final String id) {
                                  Log.d("alertdebug", "editing " + path);
                                  mModifyId = id;
                                  // requestCrop(Uri.parse(path), Uri.parse(path));

                                  mHandler.post(new Runnable() {
                                      @Override
                                      public void run() {
                                          String relativePath = Uri.parse(path).getPath();
                                          PictureEditorFloatingFragment pictureEditorFloatingFragment = new PictureEditorFloatingFragment(relativePath,mNote, id, getContext(), mServer,EditorView.this);
                                          pictureEditorFloatingFragment.setFakeFragmentManager(mFragmentManager);
                                          mFragmentManager.addFragment(pictureEditorFloatingFragment);
                                      }
                                  });

                                  //  Toast.makeText(getContext(), "editing " + path, Toast.LENGTH_SHORT).show();

                              }

                              public void askDeleteImage(String path, String id) {

                                  FalseDialogBuilder builder = new FalseDialogBuilder(getContext());
                                  builder.setText(getContext().getString(R.string.delete_confirm));
                                  builder.setOnPositiveButton(getResources().getString(android.R.string.ok), new OnClickListener() {
                                      @Override
                                      public void onClick(View view) {
                                          hideDialog();
                                      }
                                  });
                                  builder.setOnCancelButton(getResources().getString(android.R.string.cancel), new OnClickListener() {
                                      @Override
                                      public void onClick(View view) {
                                          hideDialog();
                                      }
                                  });
                                  setDialogView(builder.getView());
                                  mModifyId = id;
                                  Toast.makeText(getContext(), "deleting " + path, Toast.LENGTH_SHORT).show();

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
                            if(!mHasSet&&mNoteString!=null) {
                            /*mWebView.loadUrl("javascript:setEditorMargin('" + getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin) + "','" +
                                    getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin) + "','" +
                                    getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin) + "','" + getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin)+"')");
*/
                               onNoteAndPageReady();
                                mHasSet= true;
                            }
                        }
                    };

    private void onNoteAndPageReady() {
       // mWebView.loadUrl("javascript:loadText('" + StringEscapeUtils.escapeEcmaScript(mNoteString) + "')");
        mProgressLayout.setVisibility(GONE);
        if (!Jsoup.parse(mNoteString).text().startsWith("test" + SpiDebugUtils.testCount) && SpiDebugUtils.testCount != 0)
            Log.d("TestDebug", "ERROR " + mNoteString);
        SpiDebugUtils.testCount++;
        if (SpiDebugUtils.IS_TEST_MODE) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendAction("insertHTML", "test" + SpiDebugUtils.testCount);

                }
            }, 300);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    mRenameListener.onExit();
                }
            }, 300);

        }
    }
    // TODO: Rename method, update argument and hook method into UI event


}
