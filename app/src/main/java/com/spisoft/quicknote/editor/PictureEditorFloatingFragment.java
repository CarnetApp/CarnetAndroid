package com.spisoft.quicknote.editor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spisoft.quicknote.FloatingFragment;
import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.R;
import com.spisoft.quicknote.photoview.PhotoView;
import com.spisoft.quicknote.server.ZipReaderAndHttpProxy;
import com.spisoft.quicknote.serviceactivities.CropWrapperActivity;
import com.spisoft.quicknote.utils.FileUtils;
import com.spisoft.quicknote.utils.ZipUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by alexandre on 18/02/16.
 */
public class PictureEditorFloatingFragment implements FloatingFragment, View.OnClickListener, CropWrapperActivity.CroperResultListener {

    private final String mId;
    private final OnEditEndListener mListener;
    private final ZipReaderAndHttpProxy mServer;
    private final Note mNote;
    private Context mContext;
    private String mPath;
    private View mCropButton;
    private View mDeleteButton;
    private PhotoView mView;
    private FakeFragmentManager mFragmentManager;
    private File mOutFile;

    public void setFakeFragmentManager(FakeFragmentManager fragManager){
        mFragmentManager = fragManager;
    }
    @Override
    public void onClick(View view) {
        if(view == mCropButton){
            //first copy tmp file


            mOutFile = new File(mContext.getFilesDir(), Uri.parse(mPath).getLastPathSegment());
            mOutFile.delete();
            OutputStream outputStream = null;
            InputStream inputStream = mServer.getZipInputStream(mServer.getZipEntry(mPath));
            try {

                outputStream = new FileOutputStream(mOutFile);
                FileUtils.copy(inputStream,outputStream);

                CropWrapperActivity.startCroper(mContext, this, Uri.fromFile(mOutFile), Uri.fromFile(mOutFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(outputStream!=null)
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if (inputStream!=null)
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

            }


        }
        else if(view == mDeleteButton){
            ZipUtils.deleteEntry(mContext, mNote, mPath);
            mListener.onDelete(mId, mPath);
            mFragmentManager.removeFragment();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode== Activity.RESULT_OK) {
            if(ZipUtils.addEntry(mContext, mNote, mPath, Uri.parse(mOutFile.getAbsolutePath()))) {
                mListener.onEditEnd(mId, mPath, true);
                setPic();
            }
        }
    }

    public interface OnEditEndListener{
        public void onEditEnd(String id, String path, boolean reload);

        void onDelete(String id, String path);
    }

    public  PictureEditorFloatingFragment(String path, Note note, String id, Context context, ZipReaderAndHttpProxy server, OnEditEndListener listener){
        mContext = context;
        mId = id;
        mNote = note;
        mPath = path;
        mListener = listener;
        mServer = server;
    }
    @Override
    public View getView() {
        mView = new PhotoView(mContext);
        setPic();

        return mView;
    }

    private void setPic() {
        Log.d("picdebug", "setting pic " + mPath);
        Bitmap bmp = BitmapFactory.decodeStream(mServer.getZipInputStream(mServer.getZipEntry(mPath)));
        mView.setImageBitmap(bmp);
    }

    @Override
    public void setOptionMenu(ViewGroup container) {
        LayoutInflater.from(mContext).inflate(R.layout.picture_editor_option_menu, container);
        mCropButton = container.findViewById(R.id.crop_button);
        mCropButton.setOnClickListener(this);
        mDeleteButton = container.findViewById(R.id.delete_button);
        mDeleteButton.setOnClickListener(this);
    }
}
