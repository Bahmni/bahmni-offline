package org.bahmni.offline.dbServices.dao;

import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.JavascriptInterface;


public class ConfigDbService {
    private DbHelper mDBHelper;

    public ConfigDbService(DbHelper mDBHelper) {
        this.mDBHelper = mDBHelper;
    }


    @JavascriptInterface
    public void insertConfig(String key, String value){
        SQLiteDatabase db = mDBHelper.getWritableDatabase(Constants.KEY);
        ContentValues values = new ContentValues();
        values.put("key", key);
        values.put("value", value);
        db.insertWithOnConflict("configs", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @JavascriptInterface
    public String getConfig(String key) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
        Cursor c = db.rawQuery("SELECT * from configs" +
                " WHERE key = '" + key + "' limit 1 ", new String[]{});
        c.moveToFirst();
        return String.valueOf(new JSONObject(c.getString(c.getColumnIndex("value"))));

    }
}
