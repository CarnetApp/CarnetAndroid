package com.spisoft.quicknote.synchro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.spisoft.quicknote.synchro.googledrive.GDriveDatabase;

/**
 * Created by alexandre on 27/04/16.
 */
public class DBAccountHelper {



    private static DBAccountHelper sDBDriveFileHelper;
    private final Context mContext;
    private static final String TABLE_NAME = "DBAccount";
    private static final String KEY_RELATIVE_PATH = "key_relative_path";
    private static final String KEY_ACCOUNT = "gdrive_account";
    private static String KEY_SYNC_MD5 = "key_sync_md5";
    private static String KEY_ONLINE_LAST_MODIFY = "key_online_last_modify";

    private static final String[] COLUMNS = {
            KEY_RELATIVE_PATH,
            KEY_ACCOUNT,
            KEY_SYNC_MD5,
            KEY_ONLINE_LAST_MODIFY
    };
    public static final String CREATE_DATABASE = "create table " + TABLE_NAME + "( "
            + KEY_RELATIVE_PATH + " text not null, "
            + KEY_ACCOUNT + " long,"
            + KEY_SYNC_MD5 + " text not null, "
            + KEY_ONLINE_LAST_MODIFY + " long," +
            "PRIMARY KEY ("+KEY_RELATIVE_PATH+", "+ KEY_ACCOUNT +"));";
    public DBAccountHelper(Context context){
        mContext = context.getApplicationContext();
    }

    public static DBAccountHelper getInstance(Context context) {
        if(sDBDriveFileHelper==null)
            sDBDriveFileHelper = new DBAccountHelper(context);
        return sDBDriveFileHelper;
    }

    public DBDriveFile getDBDriveFile(long accountID, String relativePath) {
        GDriveDatabase database = GDriveDatabase.getInstance(mContext);
        DBDriveFile dbDriveFile = null;
        synchronized (database.lock) {
            SQLiteDatabase sqLiteDatabase = database.open();
            Cursor cursor = sqLiteDatabase.query(TABLE_NAME, COLUMNS, KEY_ACCOUNT + "=? AND " + KEY_RELATIVE_PATH + "= ?", new String[]{accountID + "", relativePath}, null, null, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                dbDriveFile = new DBDriveFile();
                Log.d("dbdebug",cursor.getString(cursor.getColumnIndex(KEY_RELATIVE_PATH)));
                dbDriveFile.md5 = cursor.getString(cursor.getColumnIndex(KEY_SYNC_MD5));
                dbDriveFile.relativePath = relativePath;
                dbDriveFile.accountID = accountID;
                dbDriveFile.lastOnlineModifiedDate = cursor.getLong(cursor.getColumnIndex(KEY_ONLINE_LAST_MODIFY));
            }
            cursor.close();
            database.close();
        }
        return dbDriveFile;
    }

    public DBDriveFile addOrReplaceDBDriveFile(DBDriveFile dbDriveFile) {
        GDriveDatabase database = GDriveDatabase.getInstance(mContext);
        synchronized (database.lock) {
            SQLiteDatabase sqLiteDatabase = database.open();
            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_RELATIVE_PATH, dbDriveFile.relativePath);
            initialValues.put(KEY_ACCOUNT, dbDriveFile.accountID);
            initialValues.put(KEY_SYNC_MD5, dbDriveFile.md5);
            initialValues.put(KEY_ONLINE_LAST_MODIFY, dbDriveFile.lastOnlineModifiedDate);
            long id = sqLiteDatabase.insertWithOnConflict(TABLE_NAME, null, initialValues,SQLiteDatabase.CONFLICT_REPLACE);
            dbDriveFile.accountID = id;
            database.close();
        }
        return dbDriveFile;
    }

    public void delete(DBDriveFile dbDriveFile){
        GDriveDatabase database = GDriveDatabase.getInstance(mContext);
        synchronized (database.lock) {
            SQLiteDatabase sqLiteDatabase = database.open();
            sqLiteDatabase.delete(TABLE_NAME, KEY_ACCOUNT + "=? AND " + KEY_RELATIVE_PATH + "= ?", new String[]{dbDriveFile.accountID + "", dbDriveFile.relativePath});
            database.close();
        }
    }
    public void delete(long accountID){
        GDriveDatabase database = GDriveDatabase.getInstance(mContext);
        synchronized (database.lock) {
            SQLiteDatabase sqLiteDatabase = database.open();
            sqLiteDatabase.delete(TABLE_NAME, KEY_ACCOUNT + "=?", new String[]{accountID + ""});
            database.close();
        }
    }

    public void deleteAll() {
        GDriveDatabase database = GDriveDatabase.getInstance(mContext);
        synchronized (database.lock) {
            SQLiteDatabase sqLiteDatabase = database.open();
            sqLiteDatabase.delete(TABLE_NAME, null, null);
            database.close();
        }
    }

    public static class DBDriveFile{
        public DBDriveFile(String relativePath, String md5) {
            this.relativePath = relativePath;
            this.md5 = md5;
        }
        public DBDriveFile(){}
        public String md5;
        public String relativePath;
        public long lastOnlineModifiedDate =-1;
        public long accountID;
    }

}
