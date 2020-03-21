package com.spisoft.quicknote.browser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import com.spisoft.quicknote.Note;
import com.spisoft.sync.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by phiedora on 27/07/18.
 */

public class NoteThumbnailEngine {
    private static final String TAG = "NoteThumbnailEngine";
    private final Context mContext;
    private final Handler mHandler;
    private NoteThumbnailEngineThread mThread;
    private final Stack<NoteAndViewHolder> mNoteVHStack;

    public NoteThumbnailEngine(Context context) {
        mContext = context;
        mHandler = new Handler();
        mNoteVHStack = new Stack<>();
    }

    public synchronized void addNote(Note note, NoteAdapter.NoteViewHolder viewHolder) {
        mNoteVHStack.push(new NoteAndViewHolder(note, viewHolder));
        Log.d(TAG, "add note");

        if (mThread == null || !mThread.isAlive()) {
            mThread = new NoteThumbnailEngineThread();
            mThread.start();
        }
    }

    private class NoteAndViewHolder {
        Note note;
        NoteAdapter.NoteViewHolder viewHolder;
        public NoteAndViewHolder(Note note, NoteAdapter.NoteViewHolder vh) {
            this.note = note;
            this.viewHolder = vh;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            NoteAndViewHolder that = (NoteAndViewHolder) o;

            return note != null ? note.equals(that.note) : that.note == null;
        }

        @Override
        public int hashCode() {
            return note != null ? note.hashCode() : 0;
        }
    }

    public synchronized void cancelNote(Note note) {
        mNoteVHStack.remove(new NoteAndViewHolder(note, null));
    }

    public synchronized NoteAndViewHolder popNote() {
        try {
            return mNoteVHStack.pop();
        } catch (java.util.EmptyStackException e) {
            return null;
        }
    }

    private class NoteThumbnailEngineThread extends Thread {
        @Override
        public void run() {
             NoteAndViewHolder noteAndVG;

            while ((noteAndVG = popNote()) != null) {
                final File file = new File(noteAndVG.note.path);
                Log.d(TAG, "retrieving " + noteAndVG.note.path);
                if (file.exists()) {
                    if(file.isFile()){
                        ZipFile zp = null;
                        try {
                            zp = new ZipFile(noteAndVG.note.path);
                            Enumeration<? extends ZipEntry> entries = zp.entries();
                            int i = 0;
                            Bitmap b1 = null;
                            Bitmap b2 = null;
                            while(entries.hasMoreElements() && i < 2){
                                ZipEntry entry = entries.nextElement();
                                if(entry.getName().startsWith("data/preview_")) {
                                    if(i==0) {
                                        b1 = BitmapFactory.decodeStream(zp.getInputStream(entry));
                                        if(b1 != null) {
                                            b1.setDensity(Bitmap.DENSITY_NONE);
                                            i++;
                                        }
                                    }
                                    else {
                                        b2 = BitmapFactory.decodeStream(zp.getInputStream(entry));
                                        if(b2 != null) {
                                            b2.setDensity(Bitmap.DENSITY_NONE);
                                            i++;
                                        }
                                    }

                                }
                            }


                            final Bitmap finalB2 = b2;
                            final Bitmap finalB1 = b1;
                            final NoteAndViewHolder finalNoteAndVG = noteAndVG;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    finalNoteAndVG.viewHolder.setPreview(finalNoteAndVG.viewHolder.mPreview1, finalB1, finalB2==null);
                                    finalNoteAndVG.viewHolder.setPreview(finalNoteAndVG.viewHolder.mPreview2, finalB2, true);
                                }
                            });


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        File dataFolder = new File(file, "data");
                        if(dataFolder.exists() && dataFolder.isDirectory()){
                            int i = 0;
                            Bitmap b1 = null;
                            Bitmap b2 = null;
                            for (File dataFile : dataFolder.listFiles()){
                                if(dataFile.getName().startsWith("preview_")){
                                    try {
                                        if(i==0) {
                                            b1 = BitmapFactory.decodeStream(new FileInputStream(dataFile));
                                            b1.setDensity(Bitmap.DENSITY_NONE);
                                        }
                                        else {
                                            b2 = BitmapFactory.decodeStream(new FileInputStream(dataFile));
                                            b2.setDensity(Bitmap.DENSITY_NONE);
                                        }
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    i++;
                                }
                                if(i >= 1 ) break;
                            }
                            final Bitmap finalB2 = b2;
                            final Bitmap finalB1 = b1;
                            final NoteAndViewHolder finalNoteAndVG = noteAndVG;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    finalNoteAndVG.viewHolder.setPreview(finalNoteAndVG.viewHolder.mPreview1, finalB1, finalB2==null);
                                    finalNoteAndVG.viewHolder.setPreview(finalNoteAndVG.viewHolder.mPreview2, finalB2, true);
                                }
                            });
                        }
                    }

                }
            }
        }
    }
}
