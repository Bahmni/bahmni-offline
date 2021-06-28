package org.bahmni.offline.dbServices.dao;

import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PatientDbService {

    public JSONObject getPatientByUuid(String uuid, DbHelper mDBHelper) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT patientJson, relationships from patient" +
                " WHERE uuid = '" + uuid + "' AND voided = 0 limit 1 ", new String[]{});
        if(c.getCount() < 1){
            c.close();
            return null;
        }
        c.moveToFirst();
        JSONObject result = new JSONObject();
        result.put("patient", new JSONObject(c.getString(c.getColumnIndex("patientJson"))));

        String relationships = c.getString(c.getColumnIndex("relationships"));
        if (relationships != null)
            result.put("relationships", new JSONArray(relationships));
        c.close();
        return result;
    }

    public String insertPatient(JSONObject patientData, DbHelper mDBHelper) throws JSONException {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        JSONObject patient = patientData.getJSONObject("patient");
        JSONObject person = patient.getJSONObject("person");
        JSONObject personName = (person.isNull("names") || person.getJSONArray("names").length() == 0) ? person.getJSONObject("preferredName") : person.getJSONArray("names").getJSONObject(0);
        values.put("uuid", patient.getString("uuid"));
        values.put("givenName", personName.getString("givenName"));
        if (!personName.isNull("middleName"))
            values.put("middleName", personName.getString("middleName"));
        values.put("familyName", personName.getString("familyName"));
        values.put("gender", person.getString("gender"));
        boolean isVoided = !patient.isNull("voided") && patient.getBoolean("voided");
        values.put("voided",  isVoided);
        values.put("birthdate", person.getString("birthdate"));
        values.put("dateCreated", person.getJSONObject("auditInfo").getString("dateCreated"));
        values.put("patientJson", String.valueOf(patient));
        if (!patientData.isNull("relationships"))
            values.put("relationships", String.valueOf(patientData.getJSONArray("relationships")));
        db.insertWithOnConflict("patient", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return patient.getString("uuid");
    }

}
