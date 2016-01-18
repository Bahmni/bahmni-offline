package org.bahmni.offline.db;

import android.content.ContentValues;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import org.bahmni.offline.Constants;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class MarkerDao {

    private DbHelper mDBHelper;

    public MarkerDao(DbHelper mDBHelper) {
        this.mDBHelper = mDBHelper;
    }

    public JSONObject getMarker() throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
        Cursor c = db.rawQuery("SELECT m.lastReadEventUuid, m.catchmentNumber, m.lastReadTime from marker m LIMIT 1", new String[]{});
        c.moveToFirst();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lastReadEventUuid", c.getString(c.getColumnIndex("lastReadEventUuid")));
        jsonObject.put("catchmentNumber", c.getString(c.getColumnIndex("catchmentNumber")));
        jsonObject.put("lastReadTime", c.getString(c.getColumnIndex("lastReadTime")));

        return jsonObject;
    }

    public String insertMarker(String eventUuid, String catchmentNumber) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase(Constants.KEY);
        ContentValues values = new ContentValues();
        values.put("lastReadEventUuid", eventUuid);
        values.put("catchmentNumber", catchmentNumber);
        values.put("lastReadTime", new Date().toString());

        db.insert("patient", null, values);

        return eventUuid;
    }
}
