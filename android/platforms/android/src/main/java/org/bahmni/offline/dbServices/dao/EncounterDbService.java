package org.bahmni.offline.dbServices.dao;

import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.bahmni.offline.Util;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


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
        values.put("encounterDateTime", new DateTime(encounterData.getString("encounterDateTime")).toString());
        values.put("providerUuid", encounterData.getJSONArray("providers").getJSONObject(0).getString("uuid"));
        values.put("encounterType", encounterData.getString("encounterType").toUpperCase());
        values.put("visitUuid", encounterData.getString("visitUuid"));
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
            encounterList.put(i, new JSONObject(c.getString(c.getColumnIndex("encounterJson"))));
            c.moveToNext();
        }
        c.close();
        return encounterList;
    }

    public JSONObject findActiveEncounter(JSONObject encounterDataParams, Integer encounterSessionDurationInMinutes) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
        String patientUuid = encounterDataParams.getString("patientUuid");
        String providerUuid = encounterDataParams.getString("providerUuid");
        String encounterType =  (encounterDataParams.getString("encounterType") != null) ?  encounterDataParams.getString("encounterType").toUpperCase() : null;
        DateTime encounterTime = Util.addMinutesToDate(-1 * encounterSessionDurationInMinutes, new Date());

        Cursor c = db.rawQuery("SELECT encounterJson from encounter " +
                " WHERE patientUuid = '" + patientUuid +
                "' AND providerUuid = '" + providerUuid +
                "' AND encounterType like '%" + encounterType + "%' AND DateTime(encounterDateTime) >= DateTime('" +  encounterTime + "')" , new String[] {});
        if (c.getCount() < 1) {
            c.close();
            return null;
        }
        c.moveToFirst();
        JSONObject encounter = new JSONObject(c.getString(c.getColumnIndex("encounterJson")));
        c.close();
        return  encounter;
    }

    public JSONObject findEncounterByEncounterUuid(String encounterUuid) throws JSONException{
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
        Cursor c = db.rawQuery("SELECT encounterJson from encounter" +
                " WHERE uuid = '" + encounterUuid + "'" , new String[]{});
        if (c.getCount() < 1) {
            c.close();
            return null;
        }
        c.moveToFirst();
        JSONObject encounter = new JSONObject(c.getString(c.getColumnIndex("encounterJson")));
        c.close();
        return encounter;
    }

    public JSONArray getEncountersByVisits(JSONObject params) throws JSONException{
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
        JSONArray visitUuidsArray = params.getJSONArray("visitUuids");
        String visitUuids = visitUuidsArray.toString();
        String patientUuid = params.getString("patientUuid");
        JSONArray encounterList = new JSONArray();

        String inClauseVisitUuidsList = visitUuidsArray.length() > 0 ? visitUuids.substring(2, visitUuids.length() - 2) : visitUuids;

        Cursor c = db.rawQuery("SELECT encounterJson from encounter" +
                " WHERE patientUuid = '" + patientUuid + "' AND visitUuid IN (\"" + inClauseVisitUuidsList + "\") ORDER BY encounterDateTime DESC ", new String[]{});
        if (c.getCount() < 1) {
            c.close();
            return null;
        }
        c.moveToFirst();
        Integer index=0;
        while (index < c.getCount()) {
            encounterList.put(index, new JSONObject(c.getString(c.getColumnIndex("encounterJson"))));
            c.moveToNext();
            index++;
        }
        c.close();
        return encounterList;
    }

}
