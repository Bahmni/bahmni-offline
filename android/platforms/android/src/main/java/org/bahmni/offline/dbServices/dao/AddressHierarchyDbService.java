package org.bahmni.offline.dbServices.dao;

import android.content.ContentValues;

import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.bahmni.offline.AddressField;
import org.bahmni.offline.AddressHierarchyEntry;
import org.bahmni.offline.AddressHierarchyLevel;
import org.bahmni.offline.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("id", addressHierarchy.getInt("addressHierarchyEntryId"));
        values.put("name", addressHierarchy.getString("name"));
        values.put("levelId", addressHierarchy.getJSONObject("addressHierarchyLevel").getInt("levelId"));
        if (!addressHierarchy.isNull("parentId")) {
            values.put("parentId", addressHierarchy.getInt("parentId"));
        }
        if (!addressHierarchy.isNull("userGeneratedId")) {
            values.put("userGeneratedId", addressHierarchy.getString("userGeneratedId"));
        }
        values.put("uuid", addressHierarchy.getString("uuid"));

        db.insertWithOnConflict("address_hierarchy_entry", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        return addressHierarchy;
    }

    private JSONObject insertAddressHierarchyLevel(JSONObject addressHierarchyLevel) throws JSONException {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
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

    public JSONArray search(JSONObject addressHierarchyRequest) throws JSONException {

        String searchString = null, addressFieldString = null, parentUuid = null, userGeneratedIdForParent = null;
        Integer limit = null;
        if(!addressHierarchyRequest.isNull("searchString")) {
           searchString = addressHierarchyRequest.getString("searchString");
        }
        if(!addressHierarchyRequest.isNull("addressField")) {
            addressFieldString = addressHierarchyRequest.getString("addressField");
        }
        if(!addressHierarchyRequest.isNull("limit")) {
            limit = addressHierarchyRequest.getInt("limit");
        }
        if(!addressHierarchyRequest.isNull("parentUuid")) {
            parentUuid = addressHierarchyRequest.getString("parentUuid");
        }
        if(!addressHierarchyRequest.isNull("userGeneratedIdForParent")) {
            userGeneratedIdForParent = addressHierarchyRequest.getString("userGeneratedIdForParent");
        }

        if (searchString.isEmpty() || addressFieldString.isEmpty()) {
            return null;
        }

        if (limit <= 0) {
            return null;
        }

        AddressHierarchyLevel level = getAddressHierarchyLevelByAddressField(AddressField.getByName(addressFieldString));

        if (level == null) {
            return null;
        }

        AddressHierarchyEntry parentEntry = getAddressHierarchyEntryByUuid(parentUuid);
        if (parentEntry == null) {
            parentEntry = getAddressHierarchyEntryByUserGenId(userGeneratedIdForParent);
        }

        return getAddressAsJSONArray(getAddresses(retrieveAddressHierarchyEntries(level, searchString, parentEntry, limit)));

    }


    private List<AddressHierarchyEntry> retrieveAddressHierarchyEntries(AddressHierarchyLevel level,
                                                                        String searchString, AddressHierarchyEntry parentEntry, int limit) {
        if (parentEntry != null) {
            return limit(getAddressHierarchyEntriesByLevelAndLikeNameAndParent(level, searchString, parentEntry), limit);
        }
        return getAddressHierarchyEntriesByLevelAndLikeName(level, searchString, limit);
    }

    private List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndLikeName(AddressHierarchyLevel level,
                                                                                     String searchString, int limit) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<AddressHierarchyEntry> addressHierarchyEntries = new ArrayList<AddressHierarchyEntry>();
        Cursor c = db.rawQuery("SELECT id, uuid, name, userGeneratedId, parentId FROM address_hierarchy_entry " +
                " WHERE name like '%" + searchString + "%' AND levelId = " + level.getLevelId() + " LIMIT " + limit + " ", new String[]{});
        if(c.getCount() < 1){
            c.close();
            return new ArrayList<AddressHierarchyEntry>();
        }
        c.moveToFirst();
        for(Integer i=0; i < c.getCount(); i++){
            AddressHierarchyEntry addressHierarchyEntry = new AddressHierarchyEntry();
            addressHierarchyEntry.setAddressHierarchyEntryId(c.getInt(c.getColumnIndex("id")));
            addressHierarchyEntry.setUuid(c.getString(c.getColumnIndex("uuid")));
            addressHierarchyEntry.setName(c.getString(c.getColumnIndex("name")));
            addressHierarchyEntry.setUserGeneratedId(c.getString(c.getColumnIndex("userGeneratedId")));
            addressHierarchyEntry.setParent(getAddressHierarchyEntryById(c.getInt(c.getColumnIndex("parentId"))));
            addressHierarchyEntries.add(addressHierarchyEntry);
            c.moveToNext();
        }
        c.close();
        return addressHierarchyEntries;
    }

    public AddressHierarchyEntry getAddressHierarchyEntryById(Integer id) {
        if(id != null){
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT uuid, name, userGeneratedId, parentId FROM address_hierarchy_entry " +
                    " WHERE id = '" + id + "' limit 1 ", new String[]{});
            if(c.getCount() < 1){
                c.close();
                return null;
            }
            c.moveToFirst();
            AddressHierarchyEntry addressHierarchyEntry = new AddressHierarchyEntry();
            addressHierarchyEntry.setUuid(c.getString(c.getColumnIndex("uuid")));
            addressHierarchyEntry.setName(c.getString(c.getColumnIndex("name")));
            addressHierarchyEntry.setUserGeneratedId(c.getString(c.getColumnIndex("userGeneratedId")));
            addressHierarchyEntry.setParent(getAddressHierarchyEntryById(c.getInt(c.getColumnIndex("parentId"))));
            c.close();
            return addressHierarchyEntry;
        }
        return null;
    }

    private List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndLikeNameAndParent(AddressHierarchyLevel level,
                                                                                              String searchString,
                                                                                              AddressHierarchyEntry parent) {

        if (level == null || parent == null) {
            return new ArrayList<AddressHierarchyEntry>();
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<AddressHierarchyEntry> addressHierarchyEntries = new ArrayList<AddressHierarchyEntry>();
        Cursor c = db.rawQuery("SELECT uuid, name, userGeneratedId, parentId FROM address_hierarchy_entry " +
                " WHERE name like '%" + searchString + "%' AND levelId = " + level.getLevelId() + " AND parentId = " + parent.getAddressHierarchyEntryId(), new String[]{});
        if(c.getCount() < 1){
            c.close();
            return  new ArrayList<AddressHierarchyEntry>();
        }
        c.moveToFirst();
        for(Integer i=0; i < c.getCount(); i++){
            AddressHierarchyEntry addressHierarchyEntry = new AddressHierarchyEntry();
            addressHierarchyEntry.setUuid(c.getString(c.getColumnIndex("uuid")));
            addressHierarchyEntry.setName(c.getString(c.getColumnIndex("name")));
            addressHierarchyEntry.setUserGeneratedId(c.getString(c.getColumnIndex("userGeneratedId")));
            addressHierarchyEntry.setParent(getAddressHierarchyEntryById(c.getInt(c.getColumnIndex("parentId"))));
            addressHierarchyEntries.add(addressHierarchyEntry);
            c.moveToNext();
        }
        c.close();
        return addressHierarchyEntries;

    }



    private AddressHierarchyEntry getAddressHierarchyEntryByUserGenId(String userGeneratedIdForParent) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, uuid FROM address_hierarchy_entry " +
                " WHERE userGeneratedId = '" + userGeneratedIdForParent + "' limit 1 ", new String[]{});
        if(c.getCount() < 1){
            c.close();
            return null;
        }
        c.moveToFirst();
        AddressHierarchyEntry addressHierarchyEntry = new AddressHierarchyEntry();
        addressHierarchyEntry.setAddressHierarchyEntryId(c.getInt(c.getColumnIndex("id")));
        addressHierarchyEntry.setUuid(c.getString(c.getColumnIndex("uuid")));
        c.close();
        return addressHierarchyEntry;

    }

    public AddressHierarchyEntry getAddressHierarchyEntryByUuid(String uuid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name, uuid FROM address_hierarchy_entry " +
                " WHERE uuid = '" + uuid + "' limit 1 ", new String[]{});
        if(c.getCount() < 1){
            c.close();
            return null;
        }
        c.moveToFirst();
        AddressHierarchyEntry addressHierarchyEntry = new AddressHierarchyEntry();
        addressHierarchyEntry.setAddressHierarchyEntryId(c.getInt(c.getColumnIndex("id")));
        addressHierarchyEntry.setName(c.getString(c.getColumnIndex("name")));
        addressHierarchyEntry.setUuid(c.getString(c.getColumnIndex("uuid")));
        c.close();
        return addressHierarchyEntry;

    }

    public List<AddressHierarchyLevel> getAddressHierarchyLevels() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<AddressHierarchyLevel> addressHierarchyLevelList = new ArrayList<AddressHierarchyLevel>();
        Cursor c = db.rawQuery("SELECT addressHierarchyLevelId, name, addressField FROM address_hierarchy_level", new String[]{});
        if(c.getCount() < 1){
            c.close();
            return null;
        }
        c.moveToFirst();
        for(Integer i=0; i < c.getCount(); i++){
            AddressHierarchyLevel level = new AddressHierarchyLevel();
            level.setLevelId(c.getInt(c.getColumnIndex("addressHierarchyLevelId")));
            level.setName(c.getString(c.getColumnIndex("name")));
            level.setAddressField(AddressField.valueOf(c.getString(c.getColumnIndex("addressField"))));
            addressHierarchyLevelList.add(level);
            c.moveToNext();
        }
        c.close();
        return addressHierarchyLevelList;
    }

    private ArrayList<AddressHierarchyEntry> getAddresses(List<AddressHierarchyEntry> entries) {
        ArrayList<AddressHierarchyEntry> addresses = new ArrayList<AddressHierarchyEntry>();
        if(entries != null) {
            for (AddressHierarchyEntry entry : entries) {
                addresses.add(getAddressAndParents(entry));
            }
        }
        return addresses;
    }

    private AddressHierarchyEntry getAddressAndParents(AddressHierarchyEntry entry) {
        AddressHierarchyEntry address = new AddressHierarchyEntry();
        address.setName(entry.getName());
        address.setUuid(entry.getUuid());
        address.setUserGeneratedId(entry.getUserGeneratedId());
        AddressHierarchyEntry parent = entry.getParent();
        if (parent != null) {
            address.setParent(getAddressAndParents(parent));
        }
        return address;
    }

    private AddressHierarchyLevel getAddressHierarchyLevelByAddressField(AddressField addressField) {
        if (addressField == null) {
            return null;
        }
        for (AddressHierarchyLevel level : getAddressHierarchyLevels()) {
            if (level.getAddressField() != null && level.getAddressField().equals(addressField)) {
                return level;
            }
        }
        return null;
    }

    private <T>List<T> limit(List<T> list, int limit) {
        return limit > list.size()? list: list.subList(0, limit);
    }

    private JSONArray getAddressAsJSONArray(List<AddressHierarchyEntry> addressHierarchyEntries) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for(AddressHierarchyEntry addressHierarchyEntry: addressHierarchyEntries){
            jsonArray.put(getAddressAsJSONObject(addressHierarchyEntry));
        }
        return jsonArray;
    }

    private JSONObject getAddressAsJSONObject(AddressHierarchyEntry addressHierarchyEntry) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", addressHierarchyEntry.getAddressHierarchyEntryId());
        jsonObject.put("name", addressHierarchyEntry.getName());
        jsonObject.put("userGeneratedId", addressHierarchyEntry.getUserGeneratedId());
        jsonObject.put("uuid", addressHierarchyEntry.getUuid());
        if(addressHierarchyEntry.getParent() != null) {
            jsonObject.put("parent", getAddressAsJSONObject(addressHierarchyEntry.getParent()));
        }
        return jsonObject;
    }



}
