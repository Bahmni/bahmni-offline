package org.bahmni.offline.dbServices.dao;

import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.JavascriptInterface;


public class LocationDbService {
    private DbHelper mDBHelper;

    public LocationDbService(DbHelper mDBHelper) {
        this.mDBHelper = mDBHelper;
    }

    @JavascriptInterface
    public void insertLocations(JSONArray locations) throws JSONException {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        for (int i = 0; i < locations.length(); i++) {
            ContentValues values = new ContentValues();
            values.put("uuid", locations.getJSONObject(i).getString("uuid"));
            values.put("value", String.valueOf(locations.getJSONObject(i)));
            db.insertWithOnConflict("login_locations", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    @JavascriptInterface
    public String getLocationByUuid(String uuid) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT value from login_locations" +
                " WHERE uuid = '" + uuid + "' limit 1 ", new String[]{});
        if(c.getCount() < 1){
            c.close();
            return null;
        }
        c.moveToFirst();
        JSONObject config = new JSONObject();
        config.put("value", new JSONObject(c.getString(c.getColumnIndex("value"))));
        c.close();
        return String.valueOf(config);

    }
}
