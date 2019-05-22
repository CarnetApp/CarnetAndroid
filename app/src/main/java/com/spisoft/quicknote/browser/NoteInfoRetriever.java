package com.spisoft.quicknote.browser;

import android.content.Context;
import android.os.Handler;
import android.util.Pair;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.databases.CacheManager;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.databases.RecentHelper;
import com.spisoft.quicknote.databases.RemindersManager;
import com.spisoft.sync.Log;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by phiedora on 26/07/18.
 */

public class NoteInfoRetriever {
    private static final String TAG = "NoteInfoRetriever";
    private final NoteInfoSearchHelper mNoteInfoSearchHelper;
    private Handler mHandler;
    private final Stack<String> mNoteStack;
    private final NoteInfoListener mListener;
    private final Context mContext;
    private NoteInfoRetrieverThread mThread = null;

    private NoteAdapter mNoteAdapter = null;
    public NoteInfoRetriever(NoteInfoListener listener, Context context){
        super();
        mContext = context;
        try {
            mHandler = new Handler();
        } catch(java.lang.RuntimeException e){
            mHandler = null; //not ui, so no need
        }
        mListener = listener;
        mNoteInfoSearchHelper = new NoteInfoSearchHelper(context);
        mNoteStack = new Stack<>();
    }

    public synchronized void addNote(String path){
            mNoteStack.push(path);
        Log.d(TAG, "add note");

        if (mThread == null || !mThread.isAlive()) {
                mThread = new NoteInfoRetrieverThread();
                mThread.start();
            }
    }

    public synchronized void cancelNote(String path){
            mNoteStack.remove(path);
    }

    public synchronized String popNote(){
        try {
            return mNoteStack.pop();
        } catch (java.util.EmptyStackException e){
            return null;
        }
    }

    public Note getNoteInfo(String path, String toSearch, int length){
        if(toSearch != null)
            toSearch = NoteInfoSearchHelper.cleanText(toSearch);
        Note note = new Note(path);
        ZipFile zp = null;
        try {
            zp = new ZipFile(note.path);
            Pair<String, Boolean> result = mNoteInfoSearchHelper.read(zp, zp.getEntry(NoteManager.getHtmlPath(0)), length, 10, toSearch);
            note.setShortText(result.first);
            note.hasFound(result.second);
            Note.Metadata metadata = new Note.Metadata();
            String metadataStr = mNoteInfoSearchHelper.readZipEntry(zp, zp.getEntry("metadata.json"), -1,-1, null).first;
            if(metadataStr!=null && metadataStr.length()>0){
                metadata = Note.Metadata.fromString(metadataStr);
            }
            if(!result.second){
                for(String keyword : metadata.keywords){
                    if(NoteInfoSearchHelper.cleanText(keyword).contains(toSearch)){
                        note.hasFound(true);
                        break;
                    }
                }
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
            note.file_lastmodification = new File(path).lastModified();
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
            String path;

            while((path = popNote())!=null){
                final File file = new File(path);
                Log.d(TAG, "retrieving "+path);
                if(file.exists()) {
                    Log.d(TAG, "exists");

                    Note note= getNoteInfo(path, null, 100);
                    Log.d(TAG, "getNoteInfo");
                    note.isPinned = RecentHelper.getInstance(mContext).getPinnedNotes().contains(note);

                    note.lastModified = file.lastModified();
                    note.file_lastmodification = file.lastModified();
                    if (note.mMetadata.creation_date == -1)
                        note.mMetadata.creation_date = file.lastModified();
                    if (note.mMetadata.last_modification_date == -1)
                        note.mMetadata.last_modification_date = file.lastModified();
                    note.needsUpdateInfo = false;
                    CacheManager.getInstance(mContext).addToCache(note);
                    RemindersManager.Companion.getInstance(mContext).add(note);
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



}
