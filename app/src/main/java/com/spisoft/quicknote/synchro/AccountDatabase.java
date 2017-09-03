package com.spisoft.quicknote.synchro;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.spisoft.quicknote.synchro.googledrive.DBDriveFileHelper;

/**
 * Created by alexandre on 27/04/16.
 */
public class AccountDatabase {
    public static final String DATABASE_NAME = "DBAccount";
    public static final int DATABASE_VERSION = 1;
    public static AccountDatabase sAccountDatabase=null;
    private final Context mContext;
    public static Object lock = new Object();
    private DatabaseHelper mDatabaseHelper;

    public AccountDatabase(Context context) {
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
    public static AccountDatabase getInstance(Context context){
        if(sAccountDatabase==null)
            sAccountDatabase = new AccountDatabase(context);
        return sAccountDatabase;
    }



    private class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, AccountDatabase.DATABASE_NAME, null, AccountDatabase.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // This method is only called once when the database is created for the first time
            db.execSQL(DBAccountHelper.CREATE_DATABASE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
