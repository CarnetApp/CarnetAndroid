package com.spisoft.quicknote.synchro.googledrive;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by alexandre on 27/04/16.
 */
public class GDriveDatabase {
    public static final String DATABASE_NAME = "DBGDrive";
    public static final int DATABASE_VERSION = 1;
    public static GDriveDatabase sGDriveDatabase=null;
    private final Context mContext;
    public static Object lock = new Object();
    private DatabaseHelper mDatabaseHelper;

    public GDriveDatabase(Context context) {
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
    public static GDriveDatabase getInstance(Context context){
        if(sGDriveDatabase==null)
            sGDriveDatabase = new GDriveDatabase(context);
        return sGDriveDatabase;
    }



    private class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, GDriveDatabase.DATABASE_NAME, null, GDriveDatabase.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // This method is only called once when the database is created for the first time
            db.execSQL(DBDriveFileHelper.CREATE_DATABASE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
