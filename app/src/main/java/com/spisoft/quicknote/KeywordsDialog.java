package com.spisoft.quicknote;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by phoenamandre on 09/07/17.
 */

public class KeywordsDialog extends DialogFragment implements LoaderManager.LoaderCallbacks{
    public static final String NOTE_KEY = "note_key";
    private ListView mListView;
    private CursorAdapter mAdapter;
    private Note mNote;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_keywords_dialog, container, false);

        mNote = (Note)getArguments().getSerializable(NOTE_KEY);
        getDialog().setTitle("");
        mListView = (ListView)v.findViewById(R.id.listView);
        return v;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new KeywordsLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        mAdapter = new KeywordsListAdapter(getContext(), (Cursor)data);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
