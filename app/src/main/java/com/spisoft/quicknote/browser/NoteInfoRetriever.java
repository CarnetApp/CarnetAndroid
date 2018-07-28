package com.spisoft.quicknote.browser;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.databases.CacheManager;
import com.spisoft.quicknote.databases.NoteManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by phiedora on 26/07/18.
 */

public class NoteInfoRetriever {
    private static final String TAG = "NoteInfoRetriever";
    private final NoteInfoSearchHelper mNoteInfoSearchHelper;
    private final Handler mHandler;
    private final Stack<Note> mNoteStack;
    private final NoteInfoListener mListener;
    private final Context mContext;
    private NoteInfoRetrieverThread mThread = null;

    private NoteAdapter mNoteAdapter = null;
    public NoteInfoRetriever(NoteInfoListener listener, Context context){
        super();
        mContext = context;
        mHandler = new Handler();
        mListener = listener;
        mNoteInfoSearchHelper = new NoteInfoSearchHelper(context);
        mNoteStack = new Stack<>();
    }

    public synchronized void addNote(Note note){
            mNoteStack.push(note);
        Log.d(TAG, "add note");

        if (mThread == null || !mThread.isAlive()) {
                mThread = new NoteInfoRetrieverThread();
                mThread.start();
            }
    }

    public synchronized void cancelNote(Note note){
            mNoteStack.remove(note);
    }

    public synchronized Note popNote(){
        try {
            return mNoteStack.pop();
        } catch (java.util.EmptyStackException e){
            return null;
        }
    }

    protected Note getNoteInfo(Note note){
        ZipFile zp = null;
        try {
            zp = new ZipFile(note.path);
            note.setShortText(mNoteInfoSearchHelper.read(zp, zp.getEntry(NoteManager.getHtmlPath(0)), 100, 10, null).first);

            Note.Metadata metadata = new Note.Metadata();
            String metadataStr = mNoteInfoSearchHelper.readZipEntry(zp, zp.getEntry("metadata.json"), -1,-1, null).first;
            if(metadataStr!=null && metadataStr.length()>0){
                metadata = Note.Metadata.fromString(metadataStr);
            }

            Enumeration<? extends ZipEntry> entries = zp.entries();
            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();
                Log.d(TAG, "media found "+entry.getName());

                if(entry.getName().startsWith("data/preview_")) {
                    note.previews.add(entry.getName());
                    Log.d(TAG, "preview found");
                }
            }

            note.setMetaData(metadata);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return note;
    }


    public interface NoteInfoListener{
        void onNoteInfo(Note note);
    }

    private class NoteInfoRetrieverThread extends Thread{
        @Override
        public void run(){
            Note note;

            while((note = popNote())!=null){
                final File file = new File(note.path);
                Log.d(TAG, "retrieving "+note.path);
                if(file.exists()) {
                    Log.d(TAG, "exists");

                    note= getNoteInfo(note);
                    Log.d(TAG, "getNoteInfo");

                    note.lastModified = file.lastModified();
                    if (note.mMetadata.creation_date == -1)
                        note.mMetadata.creation_date = file.lastModified();
                    if (note.mMetadata.last_modification_date == -1)
                        note.mMetadata.last_modification_date = file.lastModified();
                    note.needsUpdateInfo = false;
                    CacheManager.getInstance(mContext).addToCache(note);
                    final Note finalNote = note;
                    Log.d(TAG, "finalNote");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "sending");

                            mListener.onNoteInfo(finalNote);
                        }
                    });
                }
            }
            CacheManager.getInstance(mContext).writeCache();
        }
    }

    protected void onProgressUpdate(Note... values) {
        mNoteAdapter.setText(values[0], values[0].shortText);
    }

}
