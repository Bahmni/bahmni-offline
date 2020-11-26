package org.bahmni.offline.dbServices.dao;

import android.content.ContentValues;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

public class LabOrderDbService {
    private DbHelper mDBHelper;

    public LabOrderDbService(DbHelper mDBHelper) {
        this.mDBHelper = mDBHelper;
    }

    public String insertLabOrderResults(String patientUuid, String results) throws JSONException {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("patientUuid", patientUuid);
        values.put("labOrderResultsJson", results);

        db.insertWithOnConflict("lab_order_result", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        return results;
    }

    public JSONObject getLabOrderResultsByPatientUuid(String patientUuid) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * from lab_order_result" +
                " WHERE patientUuid = '" + patientUuid + "'", new String[]{});

        if (c.getCount() < 1) {
            c.close();
            return null;
        }

        c.moveToFirst();
        JSONObject result = new JSONObject();
        result.put("results", new JSONObject(c.getString(c.getColumnIndex("labOrderResultsJson"))));
        c.close();

        return result;
    }
}
