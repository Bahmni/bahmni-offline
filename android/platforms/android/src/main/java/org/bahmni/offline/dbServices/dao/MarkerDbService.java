package org.bahmni.offline.dbServices.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.common.base.Strings;

import org.bahmni.offline.Constants;

import net.sqlcipher.database.SQLiteDatabase;

import org.chromium.net.NetStringUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MarkerDbService {

    private DbHelper mDBHelper;

    public MarkerDbService(DbHelper mDBHelper) {
        this.mDBHelper = mDBHelper;
    }

    public JSONObject getMarker(String markerName) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * from event_log_marker where markerName = '" + markerName + "' LIMIT 1 ", new String[]{});
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

    public String insertMarker(String markerName, String eventUuid, String filters) throws JSONException  {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS zzz";
        SimpleDateFormat dateFormatter = new SimpleDateFormat(ISO_FORMAT);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateString = dateFormatter.format(new Date());

        ContentValues values = new ContentValues();
        values.put("markerName", markerName);
        values.put("lastReadEventUuid", eventUuid);
        values.put("filters", filters);
        values.put("lastReadTime", dateString);

        db.insertWithOnConflict("event_log_marker", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("markerName", markerName);
        jsonObject.put("lastReadEventUuid", eventUuid);
        jsonObject.put("filters", filters);
        jsonObject.put("lastReadTime", dateString);
        return jsonObject.toString();
    }
}
