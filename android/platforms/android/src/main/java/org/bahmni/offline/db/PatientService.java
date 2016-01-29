package org.bahmni.offline.db;

import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PatientService {

    private DbHelper mDBHelper;

    public PatientService(DbHelper mDBHelper) {
        this.mDBHelper = mDBHelper;
    }

    public JSONObject getPatientByIdentifier(String identifier) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
        Cursor c = db.rawQuery("SELECT p.identifier, p.givenName, p.familyName, p.gender, p.birthdate, p.uuid from patient p where p.identifier LIKE '%" + identifier + "%' LIMIT 1", new String[]{});
        if(c.getCount() < 1){
            c.close();
            return null;
        }
        c.moveToFirst();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("identifier", c.getString(c.getColumnIndex("identifier")));
        jsonObject.put("givenName", c.getString(c.getColumnIndex("givenName")));
        jsonObject.put("familyName", c.getString(c.getColumnIndex("familyName")));
        jsonObject.put("gender", c.getString(c.getColumnIndex("gender")));
        jsonObject.put("birthdate", c.getString(c.getColumnIndex("birthdate")));
        jsonObject.put("uuid", c.getString(c.getColumnIndex("uuid")));
        c.close();
        return jsonObject;
    }

    public JSONObject getPatientByUuid(String uuid) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
        Cursor c = db.rawQuery("SELECT * from patient" +
                " WHERE uuid = '" + uuid + "' limit 1 ", new String[]{});
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

    public String insertPatient(SQLiteDatabase db, JSONObject patientData) throws JSONException {
        ContentValues values = new ContentValues();
        JSONObject patient = patientData.getJSONObject("patient");
        JSONObject person = patient.getJSONObject("person");
        JSONObject personName = person.getJSONObject("preferredName");
        JSONArray identifiers = new JSONArray(patient.getString("identifiers"));
        String patientIdentifier = null;
        if(!identifiers.getJSONObject(0).isNull("identifier")){
            patientIdentifier = identifiers.getJSONObject(0).getString("identifier");
        }

        if (patientData.has("relationships")){
            JSONArray relationships = patientData.getJSONArray("relationships");
            if (relationships.length() > 0) {
                for (int i = 0; i < relationships.length(); i++) {
                    JSONObject relationship = relationships.getJSONObject(i);
                    JSONObject value = new JSONObject();
                    value.put("display", personName.getString("givenName") + personName.getString("familyName"));
                    value.put("uuid", patient.getString("uuid"));
                    relationship.put("personA", value);
                }
            }
        }
        values.put("identifier", patientIdentifier);
        values.put("uuid", patient.getString("uuid"));
        values.put("givenName", personName.getString("givenName"));
        if (!personName.isNull("middleName"))
            values.put("middleName", personName.getString("middleName"));
        values.put("familyName", personName.getString("familyName"));
        values.put("gender", person.getString("gender"));
        values.put("birthdate", person.getString("birthdate"));
        values.put("dateCreated", person.getJSONObject("auditInfo").getString("dateCreated"));
        values.put("patientJson", String.valueOf(patient));
        if (!patientData.isNull("relationships"))
            values.put("relationships", String.valueOf(patientData.getJSONArray("relationships")));
        db.insertWithOnConflict("patient", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return patient.getString("uuid");
    }

    public Integer generateIdentifier() throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);

        Cursor c = db.rawQuery("SELECT * from idgen limit 1 ", new String[]{});
        c.moveToFirst();
        Integer identifier = 1;

        if (c.getCount() > 0) {
            identifier = c.getInt(c.getColumnIndex("identifier"));
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("identifier", ++identifier);
        db.insertWithOnConflict("idgen", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        c.close();
        return identifier;
    }
}
