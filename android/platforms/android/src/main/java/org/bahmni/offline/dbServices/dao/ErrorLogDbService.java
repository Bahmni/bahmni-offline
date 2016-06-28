package org.bahmni.offline.dbServices.dao;

import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.JavascriptInterface;

import java.util.Date;
import java.util.UUID;

public class ErrorLogDbService {
    private DbHelper mDBHelper;

    public ErrorLogDbService(DbHelper mDBHelper){
        this.mDBHelper = mDBHelper;

    }

    @JavascriptInterface
    public void insertLog(String failedRequest, int responseStatus, String stackTrace, String requestPayload, String provider) throws JSONException {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("failedRequest", failedRequest);
        values.put("logDateTime", new Date().getTime());
        values.put("responseStatus", responseStatus);
        values.put("stackTrace", stackTrace);
        values.put("requestPayload", requestPayload);
        values.put("provider", provider);
        db.insertWithOnConflict("error_log", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public JSONArray getAllLogs() throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        JSONArray errorLogList = new JSONArray();
        Cursor c = db.rawQuery("SELECT * from error_log", new String[]{});
        if (c.getCount() < 1) {
            c.close();
            return errorLogList;
        }
        c.moveToFirst();
        for(Integer i=0; i < c.getCount(); i++){
            JSONObject errorlog = new JSONObject();
            errorlog.put("failedRequest", c.getString(c.getColumnIndex("failedRequest")));
            errorlog.put("logDateTime", c.getString(c.getColumnIndex("logDateTime")));
            errorlog.put("responseStatus", c.getInt(c.getColumnIndex("responseStatus")));
            errorlog.put("stackTrace", c.getString(c.getColumnIndex("stackTrace")));
            errorlog.put("requestPayload", c.getString(c.getColumnIndex("requestPayload")));
            errorlog.put("provider", c.getString(c.getColumnIndex("provider")));
            errorLogList.put(i, errorlog);
            c.moveToNext();
        }
        c.close();
        return errorLogList;
    }
}
