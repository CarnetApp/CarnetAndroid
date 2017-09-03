package com.spisoft.quicknote;

import android.content.Context;
import android.support.v4.content.CursorLoader;

import com.spisoft.quicknote.indexer.IndexDatabase;
import com.spisoft.quicknote.indexer.IndexerProvider;

/**
 * Created by phoenamandre on 09/07/17.
 */

public class KeywordsLoader extends CursorLoader {
    public KeywordsLoader(Context context) {
        super(context);
        setUri(IndexerProvider.getKeywordsUri());
        setProjection(IndexDatabase.COLUMNS);
        setSelection("DISTINCT("+IndexDatabase.KEY_KEYWORD+")");
    }
}
