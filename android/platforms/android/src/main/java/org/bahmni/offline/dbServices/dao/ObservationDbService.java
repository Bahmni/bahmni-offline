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
                deleteObservationByUuid(db, observationUuid, patientUuid);
            }
        }
        db.close();
        return observationData;
    }

    private void deleteObservationByUuid(SQLiteDatabase db, String observationUuid, String patientUuid) {

        db.delete("observation", "uuid =? AND patientUuid=?", new String[]{observationUuid, patientUuid});

    }

    public JSONArray getObservationsFor(JSONObject params) throws JSONException {
        String patientUuid = params.getString("patientUuid");
        JSONArray conceptNamesArray = params.getJSONArray("conceptNames");
        String conceptNames = conceptNamesArray.toString();
        JSONArray visitUuidsArray = params.getJSONArray("visitUuids");
        String visitUuids = visitUuidsArray.toString();
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
        JSONArray observations = new JSONArray();
        String inClauseConceptNameList = conceptNamesArray.length() > 0 ? conceptNames.substring(2, conceptNames.length() - 2) : conceptNames;
        String inClauseVisitUuidsList = visitUuidsArray.length() > 0 ? visitUuids.substring(2, visitUuids.length() - 2) : visitUuids;
        Cursor c = db.rawQuery("SELECT observationJson from observation" +
                " WHERE patientUuid = '" + patientUuid + "'" +
                " AND conceptName in (\"" + inClauseConceptNameList + "\") " +
                " AND ( visitUuid in (\"" + inClauseVisitUuidsList + "\") OR visitUuid is NULL )", new String[]{});
        if (c.getCount() < 1) {
            c.close();
            return new JSONArray();
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

}
