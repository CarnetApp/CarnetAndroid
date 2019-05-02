package com.spisoft.quicknote.databases

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(private val mContext: Context) {
    private var mDatabaseHelper: DatabaseHelper? = null

    fun open(): SQLiteDatabase {
        if (mDatabaseHelper == null)
            mDatabaseHelper = DatabaseHelper(mContext)
        return mDatabaseHelper!!.writableDatabase
    }

    fun close() {
        mDatabaseHelper!!.close()
    }

    private inner class DatabaseHelper internal constructor(context: Context) : SQLiteOpenHelper(context, Database.DATABASE_NAME, null, Database.DATABASE_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(RemindersManager.CREATE_TABLE)
        }
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        }
    }

    companion object {
        val DATABASE_NAME = "Carnet"
        val DATABASE_VERSION = 1
        var sDatabase: Database? = null
        var lock = Any()
        fun getInstance(context: Context): Database? {
            if (sDatabase == null)
                sDatabase = Database(context)
            return sDatabase
        }
    }
}
