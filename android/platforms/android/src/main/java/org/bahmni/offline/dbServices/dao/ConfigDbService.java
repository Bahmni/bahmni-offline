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
    public String insertConfig(String key, String value, String eTag) throws JSONException {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("key", key);
        values.put("value", value);
        values.put("etag", eTag);
        db.insertWithOnConflict("configs", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return getConfig(key);
    }

    @JavascriptInterface
    public String getConfig(String key) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * from configs" +
                " WHERE key = '" + key + "' limit 1 ", new String[]{});
        if(c.getCount() < 1){
            c.close();
            return null;
        }
        c.moveToFirst();
        JSONObject config = new JSONObject();
        config.put("value", new JSONObject(c.getString(c.getColumnIndex("value"))));
        config.put("etag", c.getString(c.getColumnIndex("etag")));
        config.put("module", c.getString(c.getColumnIndex("key")));
        c.close();
        return String.valueOf(config);

    }
}
