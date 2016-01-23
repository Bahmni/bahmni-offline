package org.bahmni.offline.db;

import android.content.ContentValues;
import android.database.Cursor;

import org.bahmni.offline.Constants;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class MarkerService {

    private DbHelper mDBHelper;

    public MarkerService(DbHelper mDBHelper) {
        this.mDBHelper = mDBHelper;
    }

    public JSONObject getMarker() throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
        Cursor c = db.rawQuery("SELECT * from event_log_marker LIMIT 1", new String[]{});
        JSONObject jsonObject = new JSONObject();

        if (c.getCount() < 1) {
            c.close();
            return null;
        }

        c.moveToFirst();
        int totalColumn = c.getColumnCount();
        System.out.println(totalColumn);
        for (int i = 0; i < totalColumn; i++) {
            if (c.getColumnName(i) != null) {
                jsonObject.put(c.getColumnName(i),
                        c.getString(i));
            }
        }
        c.close();
        return jsonObject;
    }

    public String insertMarker(String eventUuid, String catchmentNumber) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase(Constants.KEY);
        ContentValues values = new ContentValues();
        values.put("lastReadEventUuid", eventUuid);
        values.put("catchmentNumber", catchmentNumber);
        values.put("lastReadTime", new Date().toString());

        db.insertWithOnConflict("event_log_marker", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        return eventUuid;
    }
}
