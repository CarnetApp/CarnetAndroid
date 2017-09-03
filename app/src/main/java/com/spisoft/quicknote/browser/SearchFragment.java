package com.spisoft.quicknote.browser;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.spisoft.quicknote.FloatingService;
import com.spisoft.quicknote.MainActivity;
import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.R;
import com.spisoft.quicknote.databases.NoteManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alexandre on 05/02/16.
 */
public class SearchFragment extends NoteListFragment implements BrowserAdapter.OnFolderClickListener {

    private static final String PATH = "path";
    private static final String SEARCH = "search";
    private String mPath;
    private SearchAsyncTask searchTask;

    public static SearchFragment newInstance(String path, String search) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(PATH, path);
        args.putString(SEARCH, search);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    protected List<Object> getNotes() {
        mPath = getArguments().getString(PATH);

        return null;
    }
    public void doSearch(String search){
        mNoteAdapter.setNotes(new ArrayList<Object>()); //reset
        searchTask = new SearchAsyncTask();
        searchTask.execute(search);
    }

    @Override
    public void onLongClick(Note note, View v) {

    }

    public class SearchAsyncTask extends AsyncTask<String,Map.Entry<Object,Pair<String, Long>>, HashMap<Note,String>> {

        protected void onProgressUpdate(Map.Entry<Object, Pair<String, Long>>... values) {
            mNoteAdapter.addNote(values[0].getKey());
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
                    if(new File(NoteManager.getHtmlPath(0)).exists()){

                        Pair<String, Boolean> result = read(file.getAbsolutePath(), 100, 10,!nameToBeAdded?toSearch:null);
                        if(result.second||nameToBeAdded) {
                            final String txt = result.first;
                            final Note note = new Note(file.getAbsolutePath());
                            publishProgress(new Map.Entry<Object, Pair<String, Long>>() {
                                @Override
                                public Object getKey() {
                                    return note;
                                }

                                @Override
                                public Pair<String, Long> getValue() {
                                    return new Pair<String, Long>(txt, file.lastModified());
                                }

                                @Override
                                public Pair<String, Long> setValue(Pair<String, Long> s) {
                                    return new Pair<String, Long>(txt, file.lastModified());
                                }
                            });
                        }

                    }
                    else {
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
                }




            }
        }

        @Override
        protected HashMap<Note, String> doInBackground(String... lists) {


            listFiles(mPath, lists[0]);


            return null;
        }
    }

    public void onViewCreated(View v, Bundle save) {
        super.onViewCreated(v, save);
        addSecondaryButton(R.layout.browser_secondary_buttons);
        mRoot.findViewById(R.id.add_note_button).setOnClickListener(this);
        mRoot.findViewById(R.id.add_folder_button).setOnClickListener(this);
        getActivity().setTitle(R.string.browser);
        mPath = getArguments().getString(PATH);
        doSearch(getArguments().getString(SEARCH));
        //mRoot.
    }
    @Override
    public void onClick(View view) {
        if(view==mRoot.findViewById(R.id.add_button)) {
            super.onClick(view);
            /*File file = new File(PreferenceHelper.getRootPath(getActivity()));
            String path = mPath + (!mPath.endsWith("/")?"/":"")+"untitled";
            Fragment fragment = BlankFragment.newInstance(new Note(path));
            ((MainActivity) getActivity()).setFragment(fragment);*/
        }else if(view == mRoot.findViewById(R.id.add_note_button)){

            Intent intent = new Intent(getActivity(), FloatingService.class);
            intent.putExtra(FloatingService.NOTE,  NoteManager.createNewNote(mPath));
            getActivity().startService(intent);
        }else if(view == mRoot.findViewById(R.id.add_folder_button)){
            RenameDialog dialog = new RenameDialog();
            dialog.setName(getString(R.string.new_folder_name));
            dialog.setRenameListener(new RenameDialog.OnRenameListener() {
                @Override
                public boolean renameTo(String name) {
                    File newFolder = new File(mPath.endsWith("/") ? mPath : (mPath + "/") + name);
                    boolean isOk = !(newFolder).exists();
                    if (isOk) {
                        newFolder.mkdir();
                        reload();
                    }
                    return isOk;

                }
            });
            dialog.show(getFragmentManager(), "rename");
        }
    }
    public  NoteAdapter getAdapter(){
        BrowserAdapter adapter = new BrowserAdapter(getActivity(),new ArrayList<Object>());
        adapter.setOnFolderClickListener(this);


        return adapter;
    }
    @Override
    protected boolean internalOnMenuClick(MenuItem menuItem, Note note) {
        return false;
    }

    @Override
    protected void internalCreateOptionMenu(Menu menu) {

    }

    @Override
    public void onFolderClick(File folder) {
        Fragment fragment = BrowserFragment.newInstance(folder.getAbsolutePath());
        ((MainActivity)getActivity()).setFragment(fragment);
    }

    @Override
    public void onFolderOptionClick(File note, View view) {

    }

    @Override
    public void onLongClick(File note, View view) {

    }
}
