package org.bahmni.offline.db;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    private static final String TEXT_TYPE = " TEXT";
    private static final String TEXT_INTEGER = " INTEGER";
    private static final String COMMA_SEP = ",";

    private static final String SQL_DELETE_PATIENTS =
            "DROP TABLE IF EXISTS " + "patient";

    private static final String SQL_DELETE_PATIENT_ATTRIBUTES =
            "DROP TABLE IF EXISTS " + "patient_attributes";

    public DbHelper(Context context, String dbPath) {
        super(context, dbPath, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        executeSql(db, SQL_DELETE_PATIENTS);
        executeSql(db, SQL_DELETE_PATIENT_ATTRIBUTES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void createTable(SQLiteDatabase db, String sqlToCreateTable) {
        executeSql(db, sqlToCreateTable);
    }

    private void executeSql(SQLiteDatabase db, String sqlToCreateTable) {
        db.execSQL(sqlToCreateTable);
    }

    public void createIndex(SQLiteDatabase db, String sqlToCreateIndex) {
        executeSql(db, sqlToCreateIndex);
    }
}
