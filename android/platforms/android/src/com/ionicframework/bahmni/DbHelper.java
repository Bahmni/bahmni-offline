package com.ionicframework.bahmni;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

/**
 * Created by TWI on 02/11/15.
 */
public class DbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/UltraTest.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DBContract.Entry.TABLE_NAME + " (" +
                    DBContract.Entry._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    DBContract.Entry.COLUMN_PATIENT_ID + TEXT_TYPE + COMMA_SEP +
                    DBContract.Entry.COLUMN_PATIENT_UUID + TEXT_TYPE + COMMA_SEP +
                    DBContract.Entry.COLUMN_PATIENT_FIRST_NAME + TEXT_TYPE + COMMA_SEP +
                    DBContract.Entry.COLUMN_PATIENT_MIDDLE_NAME + TEXT_TYPE + COMMA_SEP +
                    DBContract.Entry.COLUMN_PATIENT_LAST_NAME + TEXT_TYPE + COMMA_SEP +
                    DBContract.Entry.COLUMN_PATIENT_JSON + TEXT_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DBContract.Entry.TABLE_NAME;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
