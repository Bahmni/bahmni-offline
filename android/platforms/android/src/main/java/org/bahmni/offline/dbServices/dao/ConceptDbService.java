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

    private static final String SETMEMBERS = "setMembers";
    private static final String RESULTS = "results";
    private static final String PARENTS = "parents";
    private static final String PARENTCONCEPTS = "parentConcepts";
    private static final String NAME = "name";
    private static final String CONCEPTNAME = "conceptName";
    private static final String UUID = "uuid";
    private static final String DATA = "data";
    private static final String ALLOBSERVATIONTEMPLATES = "All Observation Templates";

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
    }

    private void insertConcept(JSONObject data, JSONArray parent) throws JSONException {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String uuid = ((data.getJSONArray(RESULTS) != null) && (data.getJSONArray(RESULTS).get(0) != null)) ? data.getJSONArray(RESULTS).getJSONObject(0).getString(UUID) : null;
        JSONObject currentParents = getParentsByUuid(uuid);
        JSONObject parents = new JSONObject();
        JSONArray parentConcepts = new JSONArray();
        if(currentParents != null && currentParents.has(PARENTS) && currentParents.getJSONObject(PARENTS).has(PARENTCONCEPTS) && currentParents.getJSONObject(PARENTS).getJSONArray(PARENTCONCEPTS).length() > 0) {
            parentConcepts = currentParents.getJSONObject(PARENTS).getJSONArray(PARENTCONCEPTS);
        }
        for (int i = 0; i < parent.length(); i++) {
            if (!isParentAlreadyPresent(parentConcepts, parent.getString(i))) {
                parentConcepts.put(parent.getString(i));
            }
        }
        parents.put(PARENTCONCEPTS, parentConcepts);
        JSONArray childConcepts = new JSONArray();
        JSONArray childConceptNames = new JSONArray();
        if(data.getJSONArray(RESULTS).getJSONObject(0).has(SETMEMBERS)) {
            childConcepts = data.getJSONArray(RESULTS).getJSONObject(0).getJSONArray(SETMEMBERS);
        }
        for (int i = 0; i < childConcepts.length(); i++) {
            JSONObject childConcept = childConcepts.getJSONObject(i);
            JSONObject concept = new JSONObject();
            concept.put(NAME, childConcept.getJSONObject(NAME));
            concept.put(UUID, childConcept.getString(UUID));
            childConceptNames.put(concept);
        }
        data.getJSONArray(RESULTS).getJSONObject(0).put(SETMEMBERS, childConceptNames);

        ContentValues values = new ContentValues();
        values.put(DATA, data.toString());
        values.put(PARENTS,parents.toString());
        values.put(NAME, data.getJSONArray(RESULTS).getJSONObject(0).getJSONObject(NAME).getString(NAME));
        values.put(UUID, uuid);

        db.insertWithOnConflict("concept", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }


    @JavascriptInterface
    public String getConcept(String conceptUuid) throws JSONException {
        JSONObject result = findConceptByUuid(conceptUuid);
        if(result != null) {
            result = new JSONObject().put(DATA, new JSONObject().put(RESULTS, new JSONArray().put(getConceptDetailsByUuid(result))));
            JSONObject parents = getParentsByUuid(conceptUuid);
            if(parents != null) {
                result.put(PARENTS, parents.getJSONObject(PARENTS));
            }
        }
        return String.valueOf(result);
    }

    private JSONObject findConceptByUuid(String conceptUuid) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String query = "SELECT data from concept WHERE uuid = ? limit 1";
        Cursor c = db.rawQuery(query, new String[]{conceptUuid});
        if (c.getCount() < 1) {
            c.close();
            return null;
        }
        c.moveToFirst();
        JSONObject result = new JSONObject();
        result.put(DATA, new JSONObject(c.getString(c.getColumnIndex(DATA))));
        c.close();
        return result;
    }
    private JSONObject getConceptDetailsByUuid(JSONObject concept) throws JSONException {
        JSONArray setMembers = new JSONArray();
        if (concept.getJSONObject(DATA).getJSONArray(RESULTS).getJSONObject(0).has(SETMEMBERS)) {
            setMembers = concept.getJSONObject(DATA).getJSONArray(RESULTS).getJSONObject(0).getJSONArray(SETMEMBERS);
        }
        JSONArray childConcepts = new JSONArray();
        for (int i = 0; i < setMembers.length(); i++) {
            JSONObject childConcept = getConceptDetailsByUuid(findConceptByUuid(setMembers.getJSONObject(i).getString(UUID)));
            if (childConcept != null) {
                childConcepts.put(childConcept);
            }
        }
        concept.getJSONObject(DATA).getJSONArray(RESULTS).getJSONObject(0).put(SETMEMBERS, childConcepts);

        return concept.getJSONObject(DATA).getJSONArray(RESULTS).getJSONObject(0);
    }

    @JavascriptInterface
    public String getConceptByName(String conceptName) throws JSONException {
        JSONObject result = findConceptByName(conceptName);
        if(result != null) {
            if(!conceptName.equals(ALLOBSERVATIONTEMPLATES)) {
                result = new JSONObject().put(DATA, new JSONObject().put(RESULTS, new JSONArray().put(getConceptDetailsByName(result))));
            }
            JSONObject parents = getParentsByName(conceptName);
            if(parents != null) {
                result.put(PARENTS, parents.getJSONObject(PARENTS));
            }
        }
        return String.valueOf(result);
    }

    private JSONObject findConceptByName(String conceptName) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String query = "SELECT data from concept WHERE name = ? limit 1";
        Cursor c = db.rawQuery(query, new String[]{conceptName});
        if (c.getCount() < 1) {
            c.close();
            return null;
        }
        c.moveToFirst();
        JSONObject result = new JSONObject();
        result.put(DATA, new JSONObject(c.getString(c.getColumnIndex(DATA))));
        c.close();
        return result;
    }

    private JSONObject getConceptDetailsByName(JSONObject concept) throws JSONException {
        JSONArray setMembers = new JSONArray();
        if (concept.getJSONObject(DATA).getJSONArray(RESULTS).getJSONObject(0).has(SETMEMBERS)) {
            setMembers = concept.getJSONObject(DATA).getJSONArray(RESULTS).getJSONObject(0).getJSONArray(SETMEMBERS);
        }
        JSONArray childConcepts = new JSONArray();
        for (int i = 0; i < setMembers.length(); i++) {
            JSONObject childConcept = getConceptDetailsByName(findConceptByName(setMembers.getJSONObject(i).getJSONObject(NAME).getString(NAME)));
            if (childConcept != null) {
                childConcepts.put(childConcept);
            }
        }
        concept.getJSONObject(DATA).getJSONArray(RESULTS).getJSONObject(0).put(SETMEMBERS, childConcepts);

        return concept.getJSONObject(DATA).getJSONArray(RESULTS).getJSONObject(0);
    }

    private JSONObject getParentsByUuid(String conceptUuid) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String query = "SELECT parents from concept WHERE uuid = ? limit 1";
        Cursor c = db.rawQuery(query, new String[]{conceptUuid});
        if (c.getCount() < 1) {
            c.close();
            return null;
        }
        c.moveToFirst();
        JSONObject result = new JSONObject();
        result.put(PARENTS, new JSONObject(c.getString(c.getColumnIndex(PARENTS))));
        c.close();
        return result;
    }

    private JSONObject getParentsByName(String conceptName) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String query = "SELECT parents from concept WHERE name = ? limit 1";
        Cursor c = db.rawQuery(query, new String[]{conceptName});
        if (c.getCount() < 1) {
            c.close();
            return null;
        }
        c.moveToFirst();
        JSONObject result = new JSONObject();
        result.put(PARENTS, new JSONObject(c.getString(c.getColumnIndex(PARENTS))));
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
        parentObject.put(UUID, concept.get(UUID));
        parentObject.put(CONCEPTNAME, concept.getJSONObject(NAME).get(NAME));
        parent.put(parentObject);
        for (int i = 0; i < concept.getJSONArray(SETMEMBERS).length(); i++) {
            JSONObject childConcept = concept.getJSONArray(SETMEMBERS).getJSONObject(i);
            JSONObject existingConcept = findConceptByName(childConcept.getJSONObject(NAME).getString(NAME));
            if(existingConcept == null) {
                insertConcept(new JSONObject().put(RESULTS, new JSONArray().put(childConcept)), parent);
            }
            updateConceptParents(new JSONObject().put(RESULTS, new JSONArray().put(childConcept)),parent);
        }
    }

    private void updateConceptParents(JSONObject data, JSONArray parent) throws JSONException {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String uuid = ((data.getJSONArray(RESULTS) != null) && (data.getJSONArray(RESULTS).get(0) != null)) ? data.getJSONArray(RESULTS).getJSONObject(0).getString(UUID) : null;
        JSONObject currentParents = getParentsByUuid(uuid);
        JSONObject parents = new JSONObject();
        JSONArray parentConcepts = new JSONArray();
        if(currentParents != null && currentParents.getJSONObject(PARENTS).has(PARENTCONCEPTS) && currentParents.getJSONObject(PARENTS).getJSONArray(PARENTCONCEPTS).length() > 0) {
            parentConcepts = currentParents.getJSONObject(PARENTS).getJSONArray(PARENTCONCEPTS);
        }
        for (int i = 0; i < parent.length(); i++) {
            if (!isParentAlreadyPresent(parentConcepts, parent.getString(i))) {
                parentConcepts.put(parent.getString(i));
            }
        }

        parents.put(PARENTCONCEPTS, parentConcepts);
        ContentValues values = new ContentValues();

        values.put(PARENTS, parents.toString());
        db.update("concept",values,"uuid= ?",new String[] {uuid});
    }

    private JSONArray getAllParents(String childConceptName, JSONArray results) throws JSONException {
        JSONObject parents = getParentsByName(childConceptName);
        if(parents == null) {
            return results;
        }
        results.put(childConceptName);
        JSONArray parentConcepts = parents.getJSONObject(PARENTS).getJSONArray(PARENTCONCEPTS);
        if(parentConcepts.length() > 0){
            return getAllParents(new JSONObject(parentConcepts.getString(0)).getString(CONCEPTNAME), results);
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
