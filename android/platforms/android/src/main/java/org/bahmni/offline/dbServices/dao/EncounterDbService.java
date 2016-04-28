package org.bahmni.offline.dbServices.dao;

import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class EncounterDbService {
    private DbHelper mDBHelper;

    public EncounterDbService(DbHelper mDBHelper) {
        this.mDBHelper = mDBHelper;
    }

    public JSONObject insertEncounterData(JSONObject encounterData) throws JSONException {
        SQLiteDatabase db = mDBHelper.getWritableDatabase(Constants.KEY);
        ContentValues values = new ContentValues();
        values.put("uuid", encounterData.getString("encounterUuid"));
        values.put("patientUuid", encounterData.getString("patientUuid"));
        values.put("encounterDateTime", encounterData.getString("encounterDateTime"));
        values.put("encounterJson", String.valueOf(encounterData));
        db.insertWithOnConflict("encounter", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return  encounterData;
    }

    public JSONArray getEncountersByPatientUuid(String uuid) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
        JSONArray encounterList = new JSONArray();
        Cursor c = db.rawQuery("SELECT * from encounter" +
                " WHERE patientUuid = '" + uuid + "'" , new String[]{});
        if (c.getCount() < 1) {
            c.close();
            return null;
        }
        c.moveToFirst();
        for(Integer i=0; i < c.getCount(); i++){
            JSONObject encounter = new JSONObject();
            encounter.put("encounter", new JSONObject(c.getString(c.getColumnIndex("encounterJson"))));
            encounterList.put(i, encounter);
            c.moveToNext();
        }
        c.close();
        return encounterList;

    }
}
