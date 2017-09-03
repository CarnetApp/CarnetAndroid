package com.spisoft.quicknote.indexer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by alexandre on 06/07/17.
 */

public class IndexDatabase {

    private static IndexDatabase sIndexDatabase;
    private final Context mContext;
    private DatabaseHelper mDatabaseHelper;
    public static final String TABLE_NAME = "index_db";

    public static final String KEY_RELATIVE_PATH = "relative_path";
    public static final String KEY_KEYWORD = "keyword";
    public static final String KEY_FILE_SIZE = "file_size";
    public static final String KEY_MODIFICATION_DATE = "modification_date";
    public static final String DATABASE_NAME = "quickdoc_db";
    public static final String[] COLUMNS = {KEY_RELATIVE_PATH, KEY_KEYWORD, KEY_FILE_SIZE};

    public int DATABASE_VERSION = 1;

    public static final String CREATE_DATABASE = "create table " + TABLE_NAME + "( "
            + KEY_RELATIVE_PATH + " text not null, "
            + KEY_KEYWORD + " text not null, "
            + KEY_FILE_SIZE + " long,"
            + KEY_MODIFICATION_DATE + " long,"+
            "PRIMARY KEY ("+ KEY_RELATIVE_PATH +", "+ KEY_KEYWORD +"));";

    public IndexDatabase(Context context) {
        mContext = context;
    }

    public SQLiteDatabase open(){
        if(mDatabaseHelper == null)
            mDatabaseHelper = new DatabaseHelper(mContext);
        return mDatabaseHelper.getWritableDatabase();
    }

    public void close(){
        mDatabaseHelper.close();
    }
    public static IndexDatabase getInstance(Context context){
        if(sIndexDatabase==null)
            sIndexDatabase = new IndexDatabase(context);
        return sIndexDatabase;
    }



    private class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // This method is only called once when the database is created for the first time
            db.execSQL(CREATE_DATABASE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
