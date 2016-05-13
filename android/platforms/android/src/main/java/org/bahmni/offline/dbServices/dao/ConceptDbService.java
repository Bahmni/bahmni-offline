package org.bahmni.offline.dbServices.dao;

import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.JavascriptInterface;

public class ConceptDbService {
    private DbHelper mDBHelper;
    private PatientAttributeDbService patientAttributeDbService;
    private LocationDbService locationDbService;

    public ConceptDbService(DbHelper mDBHelper){
        this.mDBHelper = mDBHelper;
        patientAttributeDbService = new PatientAttributeDbService(mDBHelper);
        locationDbService = new LocationDbService(mDBHelper);
    }

    @JavascriptInterface
    public void insertConceptAndUpdateHierarchy(String data, String parent) throws JSONException {
        JSONObject dataToInsert = new JSONObject(data);
        JSONArray parentToInsert;
        if(parent == null){
            parentToInsert = new JSONArray();
        } else {
            parentToInsert = new JSONArray(parent);
        }
        insertConcept(dataToInsert, parentToInsert);
        updateChildren(dataToInsert.getJSONArray("results").getJSONObject(0));
        updateParentJson(dataToInsert.getJSONArray("results").getJSONObject(0));
    }

    private void insertConcept(JSONObject data, JSONArray parent) throws JSONException {
        SQLiteDatabase db = mDBHelper.getWritableDatabase(Constants.KEY);
        String uuid = ((data.getJSONArray("results") != null) && (data.getJSONArray("results").get(0) != null)) ? data.getJSONArray("results").getJSONObject(0).getString("uuid") : null;
        JSONObject currentParents = getParents(uuid);
        JSONObject parents = new JSONObject();
        JSONArray parentConcepts = new JSONArray();
        if(currentParents != null && currentParents.getJSONObject("parents").getJSONArray("parentConcepts").length() > 0) {
            parentConcepts = currentParents.getJSONObject("parents").getJSONArray("parentConcepts");
        }
        for (int i = 0; i < parent.length(); i++) {
            if (!isParentAlreadyPresent(parentConcepts, parent.getString(i))) {
                parentConcepts.put(parent.getString(i));
            }
        }
        parents.put("parentConcepts", parentConcepts);
        ContentValues values = new ContentValues();
        values.put("data", data.toString());
        values.put("parents", parents.toString());
        values.put("name", data.getJSONArray("results").getJSONObject(0).getJSONObject("name").getString("name"));
        values.put("uuid", uuid);
        db.insertWithOnConflict("concept", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }


    @JavascriptInterface
    public String getConcept(String conceptUuid) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
        Cursor c = db.rawQuery("SELECT * from concept" +
                " WHERE uuid = '" + conceptUuid + "' limit 1 ", new String[]{});
        if (c.getCount() < 1) {
            c.close();
            return null;
        }
        c.moveToFirst();
        JSONObject result = new JSONObject();
        result.put("data", new JSONObject(c.getString(c.getColumnIndex("data"))));
        result.put("parents", new JSONObject(c.getString(c.getColumnIndex("parents"))));
        c.close();
        return String.valueOf(result);
    }


    @JavascriptInterface
    public String getConceptByName(String conceptName) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
        Cursor c = db.rawQuery("SELECT * from concept" +
                " WHERE name = '" + conceptName + "' limit 1 ", new String[]{});
        if (c.getCount() < 1) {
            c.close();
            return null;
        }
        c.moveToFirst();
        JSONObject result = new JSONObject();
        result.put("data", new JSONObject(c.getString(c.getColumnIndex("data"))));
        result.put("parents", new JSONObject(c.getString(c.getColumnIndex("parents"))));
        c.close();
        return String.valueOf(result);
    }

    private JSONObject getParents(String conceptUuid) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
        Cursor c = db.rawQuery("SELECT parents from concept" +
                " WHERE uuid = '" + conceptUuid + "' limit 1 ", new String[]{});
        if (c.getCount() < 1) {
            c.close();
            return null;
        }
        c.moveToFirst();
        JSONObject result = new JSONObject();
        result.put("parents", new JSONObject(c.getString(c.getColumnIndex("parents"))));
        c.close();
        return result;
    }

    private boolean isParentAlreadyPresent(JSONArray parentsArray, String parent) throws JSONException {
        for (int i = 0; i < parentsArray.length(); i++) {
            if (parentsArray.getString(i).equals(parent)) {
                return true;
            }
        }
        return false;
    }

    private void updateChildren(JSONObject concept) throws JSONException {
        JSONArray parent = new JSONArray();
        JSONObject parentObject = new JSONObject();
        parentObject.put("uuid", concept.get("uuid"));
        parentObject.put("conceptName", concept.getJSONObject("name").get("name"));
        parent.put(parentObject);
        for (int i = 0; i < concept.getJSONArray("setMembers").length(); i++) {
            insertConcept(new JSONObject().put("results", new JSONArray().put(concept.getJSONArray("setMembers").getJSONObject(i))), parent);
        }
    }

    private void updateParentJson(JSONObject child) throws JSONException {
        JSONObject parents = getParents(child.getString("uuid"));
        if (parents == null || (parents!=null && parents.getJSONObject("parents").getJSONArray("parentConcepts").length() == 0))
            return;
        for (int i = 0; i < parents.getJSONObject("parents").getJSONArray("parentConcepts").length(); i++) {
            JSONObject parentConcept = new JSONObject(getConcept(new JSONObject(parents.getJSONObject("parents").getJSONArray("parentConcepts").getString(i)).getString("uuid")));
            JSONArray parentConceptSetMembers = parentConcept.getJSONObject("data").getJSONArray("results").getJSONObject(0).getJSONArray("setMembers");
            for (int j = 0; j < parentConceptSetMembers.length(); j++) {
                if (parentConceptSetMembers.getJSONObject(j).getString("uuid").equals(child.getString("uuid"))) {
                    parentConceptSetMembers.put(j, child);
                }
            }
            insertConcept(parentConcept.getJSONObject("data"), parentConcept.getJSONObject("parents").getJSONArray("parentConcepts"));
            updateParentJson(parentConcept.getJSONObject("data").getJSONArray("results").getJSONObject(0));
        }
    }


    private JSONArray getAllParents(String childConceptName, JSONArray results) throws JSONException {
        String childConcept = getConceptByName(childConceptName);
        if(childConcept == null)
            return results;
        results.put(childConceptName);
        JSONObject conceptJson = new JSONObject(childConcept);
        JSONArray parentConcepts = conceptJson.getJSONObject("parents").getJSONArray("parentConcepts");
        if(parentConcepts.length() > 0){
            return getAllParents(new JSONObject(parentConcepts.getString(0)).getString("conceptName"), results);
        }
        return results;
    }

    @JavascriptInterface
    public String getAllParentsInHierarchy(String childConceptName) throws JSONException {
        JSONArray results = getAllParents(childConceptName, new JSONArray());
        if(results.length() == 0)
            return new JSONArray().toString();
        return  results.toString();
    }
}
