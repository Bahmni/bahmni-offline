package org.bahmni.offline.dbServices.dao;


import android.content.ContentValues;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PatientIdentifierDbService {
    private DbHelper mDBHelper;

    public PatientIdentifierDbService(DbHelper mDBHelper) {
        this.mDBHelper = mDBHelper;
    }

    public void insertPatientIdentifiers(String patientUuid, JSONArray patientIdentifiers) throws JSONException {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        for (int i = 0; i < patientIdentifiers.length(); i++) {
            ContentValues values = new ContentValues();
            String identifierTypeUuid = getIdentifierType(patientIdentifiers.getJSONObject(i));
            String identifier = getIdentifier(patientIdentifiers.getJSONObject(i));
            Boolean isPrimaryIdentifier = isPrimaryIdentifier(patientIdentifiers.getJSONObject(i));

            values.put("patientUuid", patientUuid);
            values.put("identifier", identifier);
            values.put("typeUuid", identifierTypeUuid);
            values.put("isPrimaryIdentifier", isPrimaryIdentifier);
            values.put("primaryIdentifier", getExtraIdentifiers(patientIdentifiers.getJSONObject(i), "primaryIdentifier"));
            values.put("extraIdentifiers", getExtraIdentifiers(patientIdentifiers.getJSONObject(i), "extraIdentifiers"));
            values.put("identifierJson", String.valueOf(patientIdentifiers.getJSONObject(i)));
            db.insertWithOnConflict("patient_identifier", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    private String getExtraIdentifiers(JSONObject identifier, String key) throws JSONException {
        return identifier.isNull(key) ? null : identifier.getString(key);
    }

    private Boolean isPrimaryIdentifier(JSONObject identifierJson) throws JSONException {
        return identifierJson.get("identifierType") instanceof JSONObject && identifierJson.getJSONObject("identifierType").getBoolean("primary");
    }

    private String getIdentifier(JSONObject identifier) {
        String identifierValue = null;
        try {
            identifierValue = identifier.getString("identifier");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return identifierValue;
    }

    private String getIdentifierType(JSONObject identifierJson) throws JSONException {
        if (identifierJson.get("identifierType") instanceof JSONObject) {
            return identifierJson.getJSONObject("identifierType").getString("uuid");
        }
        return identifierJson.getString("identifierType");
    }

    public String getPatientIdentifiersByPatientUuid(String patientUuid) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT identifierJson from patient_identifier" +
                " WHERE patientUuid = '" + patientUuid + "' ", new String[]{});
        if(c.getCount() < 1){
            c.close();
            return null;
        }
        c.moveToFirst();
        JSONArray identifiers = new JSONArray();
        for(Integer i=0; i < c.getCount(); i++) {
            identifiers.put(new JSONObject(c.getString(c.getColumnIndex("identifierJson"))));
            c.moveToNext();
        }
        c.close();
        return String.valueOf(identifiers);
    }

}
