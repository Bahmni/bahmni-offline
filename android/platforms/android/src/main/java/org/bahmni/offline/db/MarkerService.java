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
        Cursor c = db.rawQuery("SELECT m.lastReadEventUuid, m.catchmentNumber, m.lastReadTime from event_log_marker m LIMIT 1", new String[]{});
        JSONObject jsonObject = new JSONObject();

        if(c.getCount() < 1){
            c.close();
            return null;
        }
        c.moveToFirst();
        jsonObject.put("lastReadEventUuid", c.getString(c.getColumnIndex("lastReadEventUuid")));
        jsonObject.put("catchmentNumber", c.getString(c.getColumnIndex("catchmentNumber")));
        jsonObject.put("lastReadTime", c.getString(c.getColumnIndex("lastReadTime")));
        c.close();
        return jsonObject;
    }

    public String insertMarker(String eventUuid, String catchmentNumber) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase(Constants.KEY);
        ContentValues values = new ContentValues();
        values.put("lastReadEventUuid", eventUuid);
        values.put("catchmentNumber", catchmentNumber);
        values.put("lastReadTime", new Date().toString());

        db.insertWithOnConflict("patient", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        return eventUuid;
    }
}
