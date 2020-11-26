package org.bahmni.offline.dbServices.dao;

import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.JavascriptInterface;


public class ReferenceDataDbService {
    private DbHelper mDBHelper;
    private LocationDbService locationDbService;

    public ReferenceDataDbService(DbHelper mDBHelper) {
        this.mDBHelper = mDBHelper;
        locationDbService = new LocationDbService(mDBHelper);
    }

    public void insertReferenceData(String referenceDataKey, String data, String eTag) throws JSONException {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("key", referenceDataKey);
        values.put("data", data);
        values.put("etag", eTag);
        db.insertWithOnConflict("reference_data", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        if(referenceDataKey.equals("LoginLocations")){
            locationDbService.insertLocations(new JSONObject(data).getJSONArray("results"));
        }
    }

    public String getReferenceData(String referenceDataKey) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * from reference_data" +
                " WHERE key = '" + referenceDataKey + "' limit 1 ", new String[]{});
        if(c.getCount() < 1){
            c.close();
            return null;
        }
        c.moveToFirst();
        JSONObject config = new JSONObject();
        if(referenceDataKey.equals("LocaleList") || referenceDataKey.equals("DefaultEncounterType") ||
                referenceDataKey.equals("encounterSessionDuration") || referenceDataKey.equals("NonCodedDrugConcept")) {
            config.put("data", c.getString(c.getColumnIndex("data")));
        }
        else if(referenceDataKey.equals("IdentifierSources") || referenceDataKey.equals("AddressHierarchyLevels") || referenceDataKey.equals("IdentifierTypes")) {
            config.put("data", new JSONArray(c.getString(c.getColumnIndex("data"))));
        }
        else{
            if(c.getString(c.getColumnIndex("data")).trim().equals("")){
                config.put("data", "");
            }
            else
                config.put("data", new JSONObject(c.getString(c.getColumnIndex("data"))));
        }
        config.put("etag", c.getString(c.getColumnIndex("etag")));
        config.put("key", c.getString(c.getColumnIndex("key")));
        c.close();
        return String.valueOf(config);

    }
}
