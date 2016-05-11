package org.bahmni.offline.dbServices.dao;

import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ObservationDbService {
    private DbHelper mDBHelper;

    public ObservationDbService(DbHelper mDBHelper) {
        this.mDBHelper = mDBHelper;
    }

    public JSONArray insertObservationData(String patientUuid, String visitUuid, JSONArray observationData) throws JSONException {
        SQLiteDatabase db = mDBHelper.getWritableDatabase(Constants.KEY);
        ContentValues values = new ContentValues();
        for(int index=0; index<observationData.length(); index++) {
            JSONObject observation = observationData.getJSONObject(index);
            String observationUuid = observation.getString("uuid");
            if (observation.getJSONArray("groupMembers").length() > 0) {
                values.put("uuid", observationUuid);
                values.put("patientUuid", patientUuid);
                values.put("visitUuid", visitUuid);
                values.put("conceptName", observation.getJSONObject("concept").getString("name"));
                values.put("encounterUuid", observation.getString("encounterUuid"));
                values.put("observationJson", String.valueOf(observation));
                db.insertWithOnConflict("observation", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }else{
                deleteObservationByUuid(observationUuid);
            }
        }
        return observationData;
    }

    private void deleteObservationByUuid(String observationUuid) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);

        db.beginTransaction();

        db.rawExecSQL("DELETE FROM observation WHERE uuid = '" + observationUuid + "'");

        db.setTransactionSuccessful();

        db.endTransaction();
    }

    public JSONArray getObservationsFor(JSONObject params) throws JSONException {
        String patientUuid = params.getString("patientUuid");
        String conceptNames = concatArray(params.getJSONArray("conceptNames"));
        String visitUuids = concatArray(params.getJSONArray("visitUuids"));
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
        JSONArray observations = new JSONArray();
        Cursor c = db.rawQuery("SELECT observationJson from observation" +
                " WHERE patientUuid = '" + patientUuid + "'" +
                " AND conceptName in ('" + conceptNames + "') " +
                " AND ( visitUuid in ('" + visitUuids + "') OR visitUuid is NULL )", new String[]{});
        if (c.getCount() < 1) {
            c.close();
            return null;
        }
        c.moveToFirst();
        for (int index = 0; index < c.getCount(); index++) {
            JSONObject observationJson = new JSONObject();
            observationJson.put("observation", new JSONObject(c.getString(c.getColumnIndex("observationJson"))));
            observations.put(index, observationJson);
            c.moveToNext();
        }
        c.close();
        return observations;
    }

    private String concatArray(JSONArray array) throws JSONException{
        String concatenatedString = "";
        for(int index = 0; index < array.length(); index++) {
            concatenatedString = concatenatedString + array.get(index);
            if(index < array.length()-1)
                concatenatedString += ",";
        }
        return concatenatedString;
    }


}
