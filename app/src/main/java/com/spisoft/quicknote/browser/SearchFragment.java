package com.spisoft.quicknote.browser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import java.util.List;

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
        searchTask = new SearchAsyncTask(mNoteAdapter, mPath, getContext(), mEmptyView);
        searchTask.execute(search);
    }

    @Override
    public void onLongClick(Note note, View v) {

    }


    public void onViewCreated(View v, Bundle save) {
        super.onViewCreated(v, save);
        mRoot.findViewById(R.id.add_note_button).setOnClickListener(this);
        mRoot.findViewById(R.id.add_folder_button).setVisibility(View.GONE);
        getActivity().setTitle(R.string.browser);
        mPath = getArguments().getString(PATH);
        doSearch(getArguments().getString(SEARCH));
        //mRoot.
    }
    @Override
    public void onClick(View view) {
        if(view == mRoot.findViewById(R.id.add_note_button)){

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
                        reload(mLastSelected, false);
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
    protected void internalCreateOptionMenu(Menu menu, Note note) {

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
