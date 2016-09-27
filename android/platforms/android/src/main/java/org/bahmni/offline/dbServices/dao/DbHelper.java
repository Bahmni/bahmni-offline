package org.bahmni.offline.dbServices.dao;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import org.bahmni.offline.services.EncryptionService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version. Lets say you wrote migration_8.sql then DATABASE_VERSION should be 9
    public static final int DATABASE_VERSION = 4;
    private Context myContext;

    private String encryptionKey;

    public DbHelper(Context context, String dbPath) {
        super(context, dbPath, null, DATABASE_VERSION);
        this.myContext = context;
        this.encryptionKey = new EncryptionService(context).generateKey();
    }

    public void onCreate(SQLiteDatabase db) {
        onUpgrade(db, 0, DATABASE_VERSION);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int currentVersion = oldVersion;
        String fileName;
        while(currentVersion < newVersion) {
                fileName = "migration_"+String.valueOf(currentVersion)+".sql";
                runMigration(db, fileName);
                currentVersion++;
        }
    }

    public void runMigration(SQLiteDatabase db, String filename) {
        try {
            InputStream inputStream = myContext.getAssets().open(filename);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String sqlStatements = "";
            while (bufferedReader.ready()) {
                sqlStatements += bufferedReader.readLine();
            }
            bufferedReader.close();
            db.rawExecSQL(sqlStatements);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void createTable(String sqlToCreateTable) {
        executeSql(sqlToCreateTable);
    }

    private void executeSql(String sqlToCreateTable) {
        getWritableDatabase().execSQL(sqlToCreateTable);
    }

    public SQLiteDatabase getWritableDatabase(){
        return super.getWritableDatabase(encryptionKey);
    }

    public SQLiteDatabase getReadableDatabase(){
        return super.getReadableDatabase(encryptionKey);
    }

    public void createIndex(String sqlToCreateIndex) {
        executeSql(sqlToCreateIndex);
    }
}
