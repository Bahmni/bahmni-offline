package org.bahmni.offline.dbServices.dao;

import android.content.ContentValues;

import net.sqlcipher.database.SQLiteDatabase;

import org.bahmni.offline.Constants;
import org.json.JSONException;
import org.json.JSONObject;

public class AddressHierarchyDbService {
    DbHelper dbHelper;

    public AddressHierarchyDbService(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public JSONObject insertAddressHierarchy(JSONObject addressHierarchy) throws JSONException {
        insertAddressHierarchyLevel(addressHierarchy.getJSONObject("addressHierarchyLevel"));
        return insertAddressHierarchyEntry(addressHierarchy);
    }

    private JSONObject insertAddressHierarchyEntry(JSONObject addressHierarchy) throws JSONException {
        SQLiteDatabase db = dbHelper.getWritableDatabase(Constants.KEY);
        ContentValues values = new ContentValues();

        values.put("name", addressHierarchy.getString("name"));
        values.put("levelId", addressHierarchy.getJSONObject("level").getInt("levelId"));
        if (!addressHierarchy.isNull("parent")) {
            values.put("parentId", addressHierarchy.getInt("parent"));
        }
        if (!addressHierarchy.isNull("userGeneratedId")) {
            values.put("userGeneratedId", addressHierarchy.getString("userGeneratedId"));
        }
        values.put("uuid", addressHierarchy.getString("uuid"));

        db.insertWithOnConflict("address_hierarchy_entry", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        return addressHierarchy;
    }

    private JSONObject insertAddressHierarchyLevel(JSONObject addressHierarchyLevel) throws JSONException {
        SQLiteDatabase db = dbHelper.getWritableDatabase(Constants.KEY);
        ContentValues values = new ContentValues();

        values.put("addressHierarchyLevelId", addressHierarchyLevel.getInt("levelId"));
        values.put("name", addressHierarchyLevel.getString("name"));
        if (!addressHierarchyLevel.isNull("parent")) {
            values.put("parentLevelId", addressHierarchyLevel.getInt("parent"));
        }
        values.put("addressField", addressHierarchyLevel.getString("addressField"));
        values.put("required", addressHierarchyLevel.getBoolean("required"));
        values.put("uuid", addressHierarchyLevel.getString("uuid"));

        db.insertWithOnConflict("address_hierarchy_level", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        return addressHierarchyLevel;
    }
}
