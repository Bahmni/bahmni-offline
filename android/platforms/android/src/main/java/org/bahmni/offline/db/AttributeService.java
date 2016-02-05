package org.bahmni.offline.db;

import android.content.ContentValues;
import android.database.Cursor;
import org.bahmni.offline.Util;
import net.sqlcipher.database.SQLiteDatabase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class AttributeService {

    private Util util;

    public AttributeService(Util util) {
        this.util = util;
    }

    public void insertAttributeTypes(String host, SQLiteDatabase db) throws JSONException, IOException {
        try {
            JSONArray personAttributeTypeList = new JSONObject(util.getData(new URL(host + "/openmrs/ws/rest/v1/personattributetype?v=custom:(uuid,name,format)"))).getJSONArray("results");
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

    public void insertAttributes(SQLiteDatabase db, String patientUuid, JSONArray attributes, ArrayList<JSONObject> attributeTypeMap) throws JSONException {

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
