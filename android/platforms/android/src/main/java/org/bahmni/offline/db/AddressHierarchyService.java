package org.bahmni.offline.db;

import android.content.ContentValues;

import net.sqlcipher.database.SQLiteDatabase;

import org.bahmni.offline.Constants;
import org.json.JSONException;
import org.json.JSONObject;

public class AddressHierarchyService {
    DbHelper dbHelper;

    public AddressHierarchyService(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public JSONObject insertAddressHierarchy(JSONObject addressHierarchy) throws JSONException {
        insertAddressHierarchyLevel(addressHierarchy.getJSONObject("addressHierarchyLevel"));
        return insertAddressHierarchyEntry(addressHierarchy);
    }

    private JSONObject insertAddressHierarchyEntry(JSONObject addressHierarchy) throws JSONException {
        SQLiteDatabase db = dbHelper.getWritableDatabase(Constants.KEY);
        ContentValues values = new ContentValues();

        values.put("name", addressHierarchy.getInt("name"));
        values.put("level_id", addressHierarchy.getInt("levelId"));
        values.put("parent_id", addressHierarchy.getInt("parent"));
        values.put("user_generated_id", addressHierarchy.getString("userGeneratedId"));
        values.put("uuid", addressHierarchy.getString("uuid"));

        db.insert("address_hierarchy_entry", null, values);

        return addressHierarchy;
    }

    private JSONObject insertAddressHierarchyLevel(JSONObject addressHierarchyLevel) throws JSONException {
        SQLiteDatabase db = dbHelper.getWritableDatabase(Constants.KEY);
        ContentValues values = new ContentValues();

        values.put("address_hierarchy_level_id", addressHierarchyLevel.getInt("levelId"));
        values.put("name", addressHierarchyLevel.getString("name"));
        values.put("parent_level_id", addressHierarchyLevel.getInt("parent"));
        values.put("address_field", addressHierarchyLevel.getString("addressField"));
        values.put("required", addressHierarchyLevel.getInt("required"));
        values.put("uuid", addressHierarchyLevel.getString("uuid"));

        db.insert("address_hierarchy_level", null, values);

        return addressHierarchyLevel;
    }
}
