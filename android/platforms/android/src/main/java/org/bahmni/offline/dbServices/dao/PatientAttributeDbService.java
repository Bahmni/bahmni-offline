package org.bahmni.offline.dbServices.dao;

import android.content.ContentValues;
import android.database.Cursor;
import org.bahmni.offline.Constants;
import net.sqlcipher.database.SQLiteDatabase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class PatientAttributeDbService {

    private DbHelper mDBHelper;

    public PatientAttributeDbService(DbHelper mDBHelper) {
        this.mDBHelper = mDBHelper;
    }

    public ArrayList<JSONObject> getAttributeTypes() throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
        ArrayList<JSONObject> attributeTypeMap = new ArrayList<JSONObject>();

        Cursor d = db.rawQuery("SELECT attributeTypeId, uuid, attributeName, format FROM patient_attribute_types", new String[]{});
        d.moveToFirst();
        while (!d.isAfterLast()) {
            JSONObject attributeEntry = new JSONObject();
            attributeEntry.put("attributeTypeId", d.getInt(d.getColumnIndex("attributeTypeId")));
            attributeEntry.put("uuid", d.getString(d.getColumnIndex("uuid")));
            attributeEntry.put("attributeName", d.getString(d.getColumnIndex("attributeName")));
            attributeEntry.put("format", d.getString(d.getColumnIndex("format")));
            attributeTypeMap.add(attributeEntry);
            d.moveToNext();
        }
        d.close();
        return attributeTypeMap;
    }

    public void insertAttributeTypes(String params) throws JSONException, IOException {
        SQLiteDatabase db = mDBHelper.getWritableDatabase(Constants.KEY);
        try {
            JSONArray personAttributeTypeList = new JSONArray(params);
            for (int i = 0; i < personAttributeTypeList.length(); i++) {
                ContentValues values = new ContentValues();
                values.put("attributeTypeId", String.valueOf(i));
                values.put("uuid", personAttributeTypeList.getJSONObject(i).getString("uuid"));
                values.put("attributeName", personAttributeTypeList.getJSONObject(i).getString("name"));
                values.put("format", personAttributeTypeList.getJSONObject(i).getString("format"));
                db.insertWithOnConflict("patient_attribute_types", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void insertAttributes(String patientUuid, JSONArray attributes, ArrayList<JSONObject> attributeTypeMap) throws JSONException {
        SQLiteDatabase db = mDBHelper.getWritableDatabase(Constants.KEY);
        if (attributes != null && attributes.length() > 0) {
            for (int j = 0; j < attributes.length(); j++) {
                ContentValues values = new ContentValues();
                JSONObject personAttribute = attributes.getJSONObject(j);
                if (personAttribute.isNull("voided") || (!personAttribute.isNull("voided") && !personAttribute.getBoolean("voided"))) {
                    Object object = personAttribute.get("value");
                    String value;
                    if (object instanceof JSONObject) {
                        value = ((JSONObject) object).getString("display");
                    } else
                        value = String.valueOf(object);

                    String attributeTypeId = null;

                    for (JSONObject attributeEntry : attributeTypeMap) {
                        if (attributeEntry.getString("uuid").equals(personAttribute.getJSONObject("attributeType").getString("uuid"))) {
                            attributeTypeId = attributeEntry.getString("attributeTypeId");
                        }
                    }

                    values.put("attributeTypeId", attributeTypeId);
                    values.put("attributeValue", value);
                    values.put("patientUuid", patientUuid);
                    db.insertWithOnConflict("patient_attributes", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                }
            }
        }
    }

}
