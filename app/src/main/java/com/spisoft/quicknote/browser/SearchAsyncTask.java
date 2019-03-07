package com.spisoft.quicknote.browser;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.databases.NoteManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

/**
 * Created by phiedora on 26/07/18.
 */

public class SearchAsyncTask extends AsyncTask<String,Map.Entry<Object,Pair<String, Long>>, HashMap<Note,String>> {

    private final NoteAdapter mNoteAdapter;
    private final NoteInfoRetriever mNoteInfoSearchHelper;
    private final String mPath;
    private final View mEmptyView;

    public SearchAsyncTask(NoteAdapter adapter, String path, Context context, View emptyView) {
        super();
        mNoteAdapter = adapter;
        mNoteInfoSearchHelper = new NoteInfoRetriever(null, context);
        mPath = path;
        mEmptyView = emptyView;
    }

    protected void onProgressUpdate(Map.Entry<Object, Pair<String, Long>>... values) {
        mNoteAdapter.addNote(values[0].getKey());
        mEmptyView.setVisibility(View.GONE);
        if(values[0].getKey() instanceof Note)
            mNoteAdapter.setText((Note) values[0].getKey(), values[0].getValue().first);
    }

    public void listFiles(String path, String toSearch){
        File[] files = new File(path).listFiles();
        if(files==null)
            return;
        for(final File file : files) {
            if(file.getName().startsWith("."))
                continue;
            Log.d("testdebug", "looking for "+toSearch+" in " + file.getAbsolutePath());
            boolean nameToBeAdded = file.getName().toLowerCase().contains(toSearch);


            if(file.isDirectory()) {
                if (nameToBeAdded)
                    publishProgress(new Map.Entry<Object, Pair<String, Long>>() {
                        @Override
                        public Object getKey() {
                            return file;
                        }

                        @Override
                        public Pair<String, Long> getValue() {
                            return new Pair<String, Long>(null, file.lastModified());
                        }

                        @Override
                        public Pair<String, Long> setValue(Pair<String, Long> s) {
                            return new Pair<String, Long>(null, file.lastModified());
                        }
                    });
                listFiles(file.getAbsolutePath(), toSearch);
            }
            else {
                if(file.getAbsolutePath().endsWith(".sqd")){
                    ZipFile zp = null;
                    try {
                        zp = new ZipFile(file.getAbsolutePath());
                        boolean hasFound = nameToBeAdded;
                        final Note note = mNoteInfoSearchHelper.getNoteInfo(file.getAbsolutePath(), toSearch, 100);
                        hasFound = hasFound||note.hasFound;
                        if(hasFound) {

                            publishProgress(new Map.Entry<Object, Pair<String, Long>>() {
                                @Override
                                public Object getKey() {
                                    return note;
                                }

                                @Override
                                public Pair<String, Long> getValue() {
                                    return new Pair<String, Long>(note.shortText, file.lastModified());
                                }

                                @Override
                                public Pair<String, Long> setValue(Pair<String, Long> s) {
                                    return new Pair<String, Long>(note.shortText, file.lastModified());
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }




        }
    }

    @Override
    protected HashMap<Note, String> doInBackground(String... lists) {
        listFiles(mPath, lists[0]);
        return null;
    }
}
