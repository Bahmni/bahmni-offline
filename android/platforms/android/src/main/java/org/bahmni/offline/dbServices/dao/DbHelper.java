package org.bahmni.offline.dbServices.dao;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import org.bahmni.offline.Constants;

public class DbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;

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
        executeSql(SQL_DELETE_PATIENTS);
        executeSql(SQL_DELETE_PATIENT_ATTRIBUTES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void createTable(String sqlToCreateTable) {
        executeSql(sqlToCreateTable);
    }

    private void executeSql(String sqlToCreateTable) {
        getWritableDatabase(Constants.KEY).execSQL(sqlToCreateTable);
    }

    public void createIndex(String sqlToCreateIndex) {
        executeSql(sqlToCreateIndex);
    }
}
