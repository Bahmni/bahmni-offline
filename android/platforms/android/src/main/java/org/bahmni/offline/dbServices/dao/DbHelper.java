package org.bahmni.offline.dbServices.dao;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import org.bahmni.offline.Constants;

import java.io.*;

public class DbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;

    private final Context myContext;

    public DbHelper(Context context, String dbPath) {
        super(context, dbPath, null, DATABASE_VERSION);
        myContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int currentVersion = oldVersion;
        String fileName;
        while(oldVersion <= newVersion) {
                fileName = "migrations_"+String.valueOf(currentVersion)+".sql";
                runMigration(db, fileName);
                currentVersion++;
        }
    }

    public void runMigration(SQLiteDatabase db, String filename) {
        db.beginTransaction();
        try {
            InputStream inputStream = myContext.getAssets().open(filename);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String sqlStatements = "";
            while (bufferedReader.ready()) {
                sqlStatements += bufferedReader.readLine();
            }
            bufferedReader.close();
            db.rawExecSQL(sqlStatements);
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            db.endTransaction();
        }
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
