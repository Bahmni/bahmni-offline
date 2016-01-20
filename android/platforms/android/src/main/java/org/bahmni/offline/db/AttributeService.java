package org.bahmni.offline.db;

import android.content.ContentValues;
import android.database.Cursor;
import org.bahmni.offline.Util;
import net.sqlcipher.database.SQLiteDatabase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class AttributeService {
    public void insertAttributeTypes(String host, SQLiteDatabase db) throws JSONException, IOException {
        JSONArray personAttributeTypeList = new JSONObject(Util.getData(new URL(host + "/openmrs/ws/rest/v1/personattributetype?v=custom:(name,uuid,format)"))).getJSONArray("results");
        for (int i = 0; i < personAttributeTypeList.length(); i++) {
            ContentValues values = new ContentValues();
            values.put("attributeTypeId", String.valueOf(i));
            values.put("uuid", personAttributeTypeList.getJSONObject(i).getString("uuid"));
            values.put("attributeName", personAttributeTypeList.getJSONObject(i).getString("name"));
            values.put("format", personAttributeTypeList.getJSONObject(i).getString("format"));
            db.insert("patient_attribute_types", null, values);
        }
    }

    public void insertAttributes(SQLiteDatabase db, String patientUuid, JSONArray attributes, String requestType) throws JSONException {
        Cursor d = db.rawQuery("SELECT attributeTypeId, uuid, attributeName, format FROM patient_attribute_types", new String[]{});
        d.moveToFirst();
        ArrayList<JSONObject> attributeTypeMap = new ArrayList<JSONObject>();
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
        if (requestType.equals("POST")) {
            parseAttributeValues(attributes, attributeTypeMap);
        }
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
                    db.insert("patient_attributes", null, values);
                }
            }
        }
    }

    private void parseAttributeValues(JSONArray attributes, ArrayList<JSONObject> attributeTypeMap) throws JSONException {
        for (int i = 0; i < attributes.length(); i++) {
            JSONObject attribute = attributes.getJSONObject(i);
            if (attribute.isNull("voided") || (!attribute.isNull("voided") && !attribute.getBoolean("voided"))) {
                String format = getFormat(attributeTypeMap, attribute);
                if ("java.lang.Integer".equals(format)) {
                    attribute.put("value", Integer.parseInt(attribute.getString("value")));
                }
                if ("java.lang.Float".equals(format)) {
                    attribute.put("value", Float.parseFloat(attribute.getString("value")));
                } else if ("java.lang.Boolean".equals(format)) {
                    attribute.put("value", attribute.getString("value").equals("true"));

                } else if ("org.openmrs.Concept".equals(format)) {
                    String display = attribute.getString("value");
                    JSONObject value = new JSONObject();
                    value.put("display", display);
                    value.put("uuid", attribute.getString("hydratedObject"));
                    attribute.put("value", value);
                }
            }
        }
    }

    private String getFormat(ArrayList<JSONObject> attributeTypeMap, JSONObject attribute) throws JSONException {
        for (JSONObject attributeEntry : attributeTypeMap) {
            if (attributeEntry.getString("uuid").equals(attribute.getJSONObject("attributeType").getString("uuid"))) {
                return attributeEntry.getString("format");
            }
        }
        return null;
    }
}
