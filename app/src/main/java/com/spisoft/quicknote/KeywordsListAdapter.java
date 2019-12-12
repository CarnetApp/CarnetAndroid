package com.spisoft.quicknote;

import android.content.Context;
import android.database.Cursor;
import androidx.cursoradapter.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.spisoft.quicknote.R;
import com.spisoft.quicknote.indexer.IndexDatabase;

/**
 * Created by alexandre on 10/07/17.
 */

public class KeywordsListAdapter extends CursorAdapter{
    private int mKeywordIndex;

    public KeywordsListAdapter(Context context, Cursor c) {
        super(context, c, true);
        mKeywordIndex = c.getColumnIndex(IndexDatabase.KEY_KEYWORD);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.keyword_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView)view.findViewById(R.id.textView)).setText(cursor.getString(mKeywordIndex));
    }
}
