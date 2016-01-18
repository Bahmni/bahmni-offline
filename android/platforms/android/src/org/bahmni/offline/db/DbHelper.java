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
            "DROP TABLE IF EXISTS " + "patient";

    public DbHelper(Context context, String dbPath) {
        super(context, dbPath, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_PATIENTS);
        db.execSQL(SQL_DELETE_PATIENT_ATTRIBUTES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void createTable(SQLiteDatabase db, String tableName, String[] columnNames) {
        String createTableSql = "CREATE TABLE " + tableName + " (" + "_id" + " INTEGER PRIMARY KEY";
        for (String columnName : columnNames) {
            createTableSql += COMMA_SEP + columnName + TEXT_TYPE;
        }
        createTableSql += ")";
        db.execSQL(createTableSql);
    }

    public void createIdgenTable(SQLiteDatabase db, String tableName, String columnName) {
        String createTableSql = "CREATE TABLE " + tableName + " (" + "_id" + " INTEGER PRIMARY KEY";
        createTableSql += COMMA_SEP + columnName + TEXT_INTEGER;
        createTableSql += ")";
        db.execSQL(createTableSql);
    }
}
