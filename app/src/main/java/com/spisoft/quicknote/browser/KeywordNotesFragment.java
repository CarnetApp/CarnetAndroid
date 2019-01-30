package com.spisoft.quicknote.browser;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.spisoft.quicknote.MainActivity;
import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.PreferenceHelper;
import com.spisoft.quicknote.databases.KeywordsHelper;
import com.spisoft.quicknote.databases.NoteManager;
import com.spisoft.quicknote.editor.BlankFragment;
import com.spisoft.quicknote.utils.SpiDebugUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by alexandre on 03/02/16.
 */
public class KeywordNotesFragment extends NoteListFragment {
    private Note mTestNote;
    private static final String EXTRA_KEYWORD = "keyword";
    public KeywordNotesFragment(){
        super();
    }

    public static KeywordNotesFragment newInstance(String keyword) {
        KeywordNotesFragment fragment = new KeywordNotesFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_KEYWORD, keyword);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
    protected  void onReady(){
        if(SpiDebugUtils.IS_TEST_MODE){
            if(mTestNote == null)
                mTestNote = NoteManager.createNewNote(PreferenceHelper.getRootPath(getActivity()));
        ((MainActivity)getActivity()).setFragment(BlankFragment.newInstance(mTestNote, null));

        }

    }
    @Override
    protected List<Object> getNotes() {
        Map<String,List<String>> keywords =  KeywordsHelper.getInstance(getActivity()).getFlattenDB(-1);
        List<Object> notes = new ArrayList<>();
        for (String path : keywords.get(getArguments().getString(EXTRA_KEYWORD))){
            notes.add(new Note(PreferenceHelper.getRootPath(getActivity())+"/"+path));
        }
        return notes;
    }

    @Override
    protected boolean internalOnMenuClick(MenuItem menuItem, Note note) {

        return false;
    }

    @Override
    protected void internalCreateOptionMenu(Menu menu, Note note) {
    }

    @Override
    public void onLongClick(Note note, View v) {

    }
}
