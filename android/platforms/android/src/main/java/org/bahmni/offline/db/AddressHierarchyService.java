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
        values.put("levelId", addressHierarchy.getInt("levelId"));
        values.put("parentId", addressHierarchy.getInt("parent"));
        values.put("userGeneratedId", addressHierarchy.getString("userGeneratedId"));
        values.put("uuid", addressHierarchy.getString("uuid"));

        db.insertWithOnConflict("address_hierarchy_entry", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        return addressHierarchy;
    }

    private JSONObject insertAddressHierarchyLevel(JSONObject addressHierarchyLevel) throws JSONException {
        SQLiteDatabase db = dbHelper.getWritableDatabase(Constants.KEY);
        ContentValues values = new ContentValues();

        values.put("address_hierarchy_level_id", addressHierarchyLevel.getInt("levelId"));
        values.put("name", addressHierarchyLevel.getString("name"));
        values.put("parentLevelId", addressHierarchyLevel.getInt("parent"));
        values.put("addressField", addressHierarchyLevel.getString("addressField"));
        values.put("required", addressHierarchyLevel.getInt("required"));
        values.put("uuid", addressHierarchyLevel.getString("uuid"));

        db.insertWithOnConflict("address_hierarchy_level", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        return addressHierarchyLevel;
    }
}
