package com.spisoft.quicknote.indexer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by alexandre on 06/07/17.
 */

public class IndexerProvider extends ContentProvider {
    private static String AUTHORITY = "spidocs";
    private static final UriMatcher URI_MATCHER =
            new UriMatcher(UriMatcher.NO_MATCH);
    private static final int KEYWORDS = 1;
    private static final int KEYWORD = 2;

    static {
        URI_MATCHER.addURI(AUTHORITY, "keywords", KEYWORDS);
        URI_MATCHER.addURI(AUTHORITY, "keywords/#", KEYWORD);

    }

    public static Uri getKeywordsUri(){
        return Uri.parse("content://"+AUTHORITY+"/keywords");
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projectionIn, String selection, String[] selectionArgs,
                        String sort) {
        int match = URI_MATCHER.match(uri);

        switch (match) {
            case KEYWORDS:{
                Cursor ret = IndexDatabase.getInstance(getContext()).open().query(IndexDatabase.TABLE_NAME, projectionIn, selection, selectionArgs, null, null, sort, null);
                IndexDatabase.getInstance(getContext()).close();
                return ret;
            }
            case KEYWORD:
                ArrayList<String> args = new ArrayList<>();
                if(selectionArgs!=null)
                    args.addAll(Arrays.asList(selectionArgs));

                if(selection == null) {
                    selection = "";
                }
                if(!selection.isEmpty()) {
                    selection = " AND (" + selection + ")";
                }
                selection = IndexDatabase.KEY_KEYWORD+" = ?"+selection;
                args.add(0, uri.getLastPathSegment());
                Cursor ret = IndexDatabase.getInstance(getContext()).open().query(IndexDatabase.TABLE_NAME, projectionIn, selection, args.toArray(new String[0]), null, null, sort, null);
                IndexDatabase.getInstance(getContext()).close();
                return ret;
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int match = URI_MATCHER.match(uri);

        switch (match) {
            case KEYWORDS:{
                IndexDatabase.getInstance(getContext()).open().insert(IndexDatabase.TABLE_NAME, null, contentValues);
                IndexDatabase.getInstance(getContext()).close();
                return uri;
            }
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
