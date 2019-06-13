package com.spisoft.quicknote.editor;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.cameraview.CameraView;
import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.R;
import com.spisoft.quicknote.databases.KeywordsHelper;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.databases.RecentHelper;
import com.spisoft.quicknote.utils.CustomOrientationEventListener;
import com.spisoft.quicknote.utils.FileUtils;
import com.spisoft.quicknote.utils.PictureUtils;
import com.spisoft.quicknote.utils.ZipUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static com.spisoft.quicknote.databases.NoteManager.PREVIEW_HEIGHT;
import static com.spisoft.quicknote.databases.NoteManager.PREVIEW_WIDTH;

public class ImageActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private CameraView mCameraView;
    private View mShootButton;
    private File mTmpDir;
    private CameraView.Callback mCallback = new CameraView.Callback() {
        @Override
        public void onCameraOpened(CameraView cameraView) {
            super.onCameraOpened(cameraView);
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            super.onCameraClosed(cameraView);
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            super.onPictureTaken(cameraView, data);
            setOrientation(0);
            mCameraView.animate().alpha(1).setDuration(100).start();
            new AsyncTask<Void, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(Void... voids) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    return bitmap;
                }
                protected void onPostExecute(Bitmap b) {
                    if(b != null)
                        handlePhotoBitmap(b);


                }


            }.execute();

        }
    };
    private LinearLayout mImageListView;
    private LinearLayout mAvailableKeywordsLinearLayout;
    private List<String> mAvailableKeywords;
    private LinearLayout mSelectedKeywordsLinearLayout;
    private EditText mKeywordET;
    private View mCreateButton;
    private View mNextButton;
    private ImageButton mFlashButton;
    private View mExternalCameraButton;
    private CustomOrientationEventListener customOrientationEventListener;
    static final SparseIntArray DISPLAY_ORIENTATIONS = new SparseIntArray();

    static {
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_0, 0);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_90, 90);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_180, 180);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_270, 270);
    }

    private int mCurrentOrientation = 0;
    private boolean mHasAlreadyAsked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTmpDir = new File(this.getCacheDir().getAbsolutePath()+"/image_dir");
        if(mTmpDir.exists()){
            //delete
            FileUtils.deleteRecursive(mTmpDir);
        }
        mTmpDir.mkdirs();
        setContentView(R.layout.activity_image);
        mAvailableKeywords = new ArrayList<>();
        mAvailableKeywordsLinearLayout = findViewById(R.id.available_keywords);
        mSelectedKeywordsLinearLayout = findViewById(R.id.selected_keywords);
        mKeywordET = findViewById(R.id.keyword);
        mKeywordET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshAvailableKeywords();
            }
        });
        mImageListView = findViewById(R.id.image_list);
        mCameraView = findViewById(R.id.camera);
        mCameraView.addCallback(mCallback);
        mCameraView.setFlash(PreferenceManager.getDefaultSharedPreferences(this).getInt("last_camera_flash", CameraView.FLASH_TORCH));
        mExternalCameraButton = findViewById(R.id.external_camera_button);
        mExternalCameraButton.setOnClickListener(this);
        mNextButton = findViewById(R.id.next);
        mNextButton.setOnClickListener(this);
        mFlashButton = findViewById(R.id.flash_button);
        mFlashButton.setOnClickListener(this);
        refreshFlashButton();
        mShootButton = findViewById(R.id.shoot_button);
        mShootButton.setOnClickListener(this);
        mCreateButton = findViewById(R.id.create_button);
        mCreateButton.setOnClickListener(this);
        try {
            JSONObject keywordsJson = KeywordsHelper.getInstance(this).getJson();
            JSONArray array = keywordsJson.getJSONArray("data");
            if(array.length() >0)
            for(int i = array.length() -1; i>=0; i--) {
                String action = array.getJSONObject(i).getString("action");
                if(!action.equals("move")&&!action.equals("delete")) {
                    String keyword = array.getJSONObject(i).getString("keyword");
                    if(!mAvailableKeywords.contains(keyword))
                        mAvailableKeywords.add(keyword);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refreshAvailableKeywords();


    }

    private void refreshAvailableKeywords() {
        mAvailableKeywordsLinearLayout.removeAllViews();
        String text = mKeywordET.getText().toString();
        if(text.isEmpty())
        for(int i = 0; i<5 && i < mAvailableKeywords.size(); i ++){
            mAvailableKeywordsLinearLayout.addView(createKeywordView(mAvailableKeywords.get(i)));
        }
        else{
            mAvailableKeywordsLinearLayout.addView(createKeywordView(text));
            for(int i = 0; i < mAvailableKeywords.size(); i ++){
                if(mAvailableKeywordsLinearLayout.getChildCount() > 5)
                    break;
                if(mAvailableKeywords.get(i).toLowerCase().startsWith(text.toLowerCase()))
                    mAvailableKeywordsLinearLayout.addView(createKeywordView(mAvailableKeywords.get(i)));
            }
        }
    }

    private View createKeywordView(String s) {
        CheckBox view = new CheckBox(this);
        view.setText(s);
        view.setOnCheckedChangeListener(this);
        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        new Thread(){
            public void run(){
                if(mCameraView.isCameraOpened())
                    mCameraView.stop();
            }

        }.start();
    }
    @Override
    public void onResume(){
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            if(mHasAlreadyAsked)
                finish();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1001);
            mHasAlreadyAsked = true;

        }
        else mCameraView.start();
        customOrientationEventListener = new CustomOrientationEventListener(this) {
            @Override
            public void onSimpleOrientationChanged(int orientation) {
                if(orientation == 1)
                    orientation = 3;
                else if(orientation == 3)
                    orientation = 1;
                mCurrentOrientation = orientation;

            }
        };

        customOrientationEventListener.enable();

    }

    private void setOrientation(int orientation){
        try {
            Field f = CameraView.class.getDeclaredField("mDisplayOrientationDetector");
            f.setAccessible(true);
            Object mDisplayOrientationDetector = f.get(mCameraView);
            mDisplayOrientationDetector.getClass().getDeclaredMethod("onDisplayOrientationChanged", int.class).setAccessible(true);
            mDisplayOrientationDetector.getClass().getDeclaredMethod("onDisplayOrientationChanged", int.class).invoke(mDisplayOrientationDetector, DISPLAY_ORIENTATIONS.get(orientation));
            mCameraView.setRotation(DISPLAY_ORIENTATIONS.get(orientation));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void onPause(){
        super.onPause();
        customOrientationEventListener.disable();
        new Thread(){
            public void run(){
                if(mCameraView.isCameraOpened())
                    mCameraView.stop();
            }

        }.start();
    }
    private void refreshFlashButton(){
        if(mCameraView.getFlash() == CameraView.FLASH_OFF){
            mFlashButton.setImageResource(R.drawable.flash_off);
        } else {
            mFlashButton.setImageResource(R.drawable.flash_on);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1002 && resultCode == RESULT_OK) {
            handlePhotoBitmap((Bitmap) data.getExtras().get("data"));
        }
    }

    private void handlePhotoBitmap(final Bitmap bitmap) {
        new AsyncTask<Void, Void, View>() {
            @Override
            protected View doInBackground(Void... voids) {
                File dataF = new File(mTmpDir, "data/");
                dataF.mkdirs();
                File file = new File(dataF, System.currentTimeMillis() + ".jpg");
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    bitmap.recycle();
                    ImageView v = new ImageView(ImageActivity.this);
                    v.setAdjustViewBounds(true);
                    File preview = new File(dataF, "preview_" + file.getName()+ ".jpg"); //previews have twice .jpg .... not changing that otherwise deleted from editor will fail...

                    try {
                        PictureUtils.resize(file.getAbsolutePath(), preview.getAbsolutePath(), PREVIEW_WIDTH, PREVIEW_HEIGHT);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    v.setImageURI(Uri.fromFile(preview));
                    v.setTag(file.getName());
                    v.setOnClickListener(ImageActivity.this);
                    v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    return v;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return null;
            }
            protected void onPostExecute(View v) {
                if(v!=null)
                    mImageListView.addView(v);
                mNextButton.setEnabled(true);
            }
        }.execute();

    }


    @Override
    public void onClick(final View v) {
        if(v == mFlashButton){
            if(mCameraView.getFlash() == CameraView.FLASH_OFF){
                mCameraView.setFlash(CameraView.FLASH_TORCH);
            } else {
                mCameraView.setFlash(CameraView.FLASH_OFF);
            }
            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("last_camera_flash", mCameraView.getFlash()).apply();
            refreshFlashButton();
        }
        else if (v ==  mExternalCameraButton){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, 1002);
            }

        }
        else if(v == mShootButton){
            mCameraView.animate().alpha(0).setDuration(100).withEndAction(new Runnable() {
                @Override
                public void run() {
                    setOrientation(mCurrentOrientation);
                    mCameraView.takePicture();
                }
            }).start();


        }
        else if(v == mNextButton){
            findViewById(R.id.note_params).setVisibility(View.VISIBLE);
            findViewById(R.id.photo_taking_container).setVisibility(View.GONE);
            new Thread(){
                public void run(){
                    if(mCameraView.isCameraOpened())
                        mCameraView.stop();
                }

            }.start();
        }
        else if(v == mCreateButton){
            Note.Metadata metadata = new Note.Metadata();
            metadata.creation_date = System.currentTimeMillis();
            metadata.last_modification_date = System.currentTimeMillis();
            List <String> keywords = new ArrayList<>();
            for(int i = 0; i < mSelectedKeywordsLinearLayout.getChildCount(); i++){
                keywords.add(((TextView)mSelectedKeywordsLinearLayout.getChildAt(i)).getText().toString());
            }
            metadata.keywords.addAll(keywords);
            FileUtils.writeToFile(new File(mTmpDir,"metadata.json").getAbsolutePath(), metadata.toJsonObject().toString());
            FileUtils.writeToFile(new File(mTmpDir,"index.html").getAbsolutePath(), NoteManager.getDefaultHTML());
            if(((CheckBox) findViewById(R.id.one_note_per_photo_cb)).isChecked()){
                File[] images = new File(mTmpDir, "data/").listFiles();
                if(images != null){
                    for (File image : images){
                        if(image.getName().startsWith("preview_"))
                            continue;

                        File noteFolder = new File(mTmpDir, "note");
                        if(noteFolder.exists())
                            FileUtils.deleteRecursive(noteFolder);

                        File data = new File(noteFolder, "data/");
                        data.mkdirs();
                        image.renameTo(new File(data, image.getName()));
                        File preview = new File(image.getParentFile(), "preview_"+image.getName() + ".jpg"); //previews have twice .jpg .... not changing that otherwise deleted from editor will fail...
                        if(preview.exists()){
                            preview.renameTo(new File(data, preview.getName()));
                        }
                        try {
                            FileUtils.copy(new FileInputStream(new File(mTmpDir, "index.html")),new FileOutputStream(new File(noteFolder, "index.html")));
                            FileUtils.copy(new FileInputStream(new File(mTmpDir, "metadata.json")),new FileOutputStream(new File(noteFolder, "metadata.json")));
                            Note note = NoteManager.createNewNote(PreferenceHelper.getRootPath(this));
                            ZipUtils.zipFolder(noteFolder, note.path,new ArrayList<String>());
                            RecentHelper.getInstance(this).addNote(note);
                            for(String keyword : keywords) {
                                KeywordsHelper.getInstance(this).addKeyword(keyword, note);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(noteFolder.exists())
                            FileUtils.deleteRecursive(noteFolder);
                    }
                }
            }
            else{
                Note note = NoteManager.createNewNote(PreferenceHelper.getRootPath(this));
                ZipUtils.zipFolder(mTmpDir, note.path,new ArrayList<String>());
                RecentHelper.getInstance(this).addNote(note);
                for(String keyword : keywords) {
                    KeywordsHelper.getInstance(this).addKeyword(keyword, note);
                }

            }
            setResult(RESULT_OK);
            finish();

        } else if(v instanceof ImageView){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.delete_confirm)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String name = (String) v.getTag();
                            new File(mTmpDir, "data/"+name).delete();
                            new File(mTmpDir, "data/preview_"+name + ".jpg").delete(); //previews have twice .jpg .... not changing that otherwise deleted from editor will fail...
                            ((LinearLayout)v.getParent()).removeView(v);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();

        }
    }

    @Override
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
        buttonView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isChecked){
                    mAvailableKeywordsLinearLayout.removeView(buttonView);
                    mSelectedKeywordsLinearLayout.addView(buttonView);
                } else{
                    mSelectedKeywordsLinearLayout.removeView(buttonView);
                    mAvailableKeywordsLinearLayout.addView(buttonView);
                }
            }
        },500);


    }
}
