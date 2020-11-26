package org.bahmni.offline.dbServices.dao;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import org.bahmni.offline.services.EncryptionService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DbHelper extends SQLiteOpenHelper {
    //If you change the database schema, you must increment the database version.
    //Lets say you wrote migration_8.sql for Location specific Db then LOCATION_DB_VERSION should be 9 in Constants.java
    //If some migration need to be done in "metaData" db then METADATA_DB_VERSION in Constants should be upgraded/
    public int CURRENT_DB_VERSION;
    public String dbPath;
    private Context myContext;

    private String encryptionKey;

    public DbHelper(Context context, String dbPath, int dbVersion) {
        super(context, dbPath, null, dbVersion);
        this.dbPath = dbPath;
        this.CURRENT_DB_VERSION = dbVersion;
        this.myContext = context;
        this.encryptionKey = new EncryptionService(context).generateKey();
    }

    public void onCreate(SQLiteDatabase db) {
        onUpgrade(db, 0, this.CURRENT_DB_VERSION);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int currentVersion = oldVersion;
        String migrationFileName = this.dbPath.contains("metaData") ? "metadata_migration_" : "migration_";
        String fileName;
        while(currentVersion < newVersion) {
                fileName = migrationFileName+String.valueOf(currentVersion)+".sql";
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
            sqlStatements = replaceParameters(sqlStatements);
            bufferedReader.close();
            db.rawExecSQL(sqlStatements);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String replaceParameters(String sqlStatements) {
        if(sqlStatements.contains("@")) {
            String metaDataDbPath = myContext.getExternalFilesDir(null) + "/metaData.db";
            sqlStatements = sqlStatements.replace("@metaDataDbPath", "'"+ metaDataDbPath + "'");
            sqlStatements = sqlStatements.replace("@encryptionKey", "'"+ this.encryptionKey +"'");
        }
        return sqlStatements;
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
