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
    private PatientAttributeDbService patientAttributeDbService;
    private LocationDbService locationDbService;

    public ReferenceDataDbService(DbHelper mDBHelper) {
        this.mDBHelper = mDBHelper;
        patientAttributeDbService = new PatientAttributeDbService(mDBHelper);
        locationDbService = new LocationDbService(mDBHelper);
    }


    @JavascriptInterface
    public void insertReferenceData(String referenceDataKey, String data, String eTag) throws JSONException {
        SQLiteDatabase db = mDBHelper.getWritableDatabase(Constants.KEY);
        ContentValues values = new ContentValues();
        values.put("key", referenceDataKey);
        values.put("value", data);
        values.put("etag", eTag);
        db.insertWithOnConflict("reference_data", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        if(referenceDataKey.equals("PersonAttributeType")){
            patientAttributeDbService.insertAttributeTypes(String.valueOf(new JSONObject(data).getJSONArray("results")));
        }
        if(referenceDataKey.equals("LoginLocations")){
            locationDbService.insertLocations(new JSONObject(data).getJSONArray("results"));
        }
    }

    @JavascriptInterface
    public String getReferenceData(String referenceDataKey) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
        Cursor c = db.rawQuery("SELECT * from reference_data" +
                " WHERE key = '" + referenceDataKey + "' limit 1 ", new String[]{});
        if(c.getCount() < 1){
            c.close();
            return null;
        }
        c.moveToFirst();
        JSONObject config = new JSONObject();
        if(referenceDataKey.equals("LocaleList")) {
            config.put("value", c.getString(c.getColumnIndex("value")));
        }
        else if(referenceDataKey.equals("IdentifierSources") || referenceDataKey.equals("AddressHierarchyLevels")) {
            config.put("value", new JSONArray(c.getString(c.getColumnIndex("value"))));
        }
        else{
            if(c.getString(c.getColumnIndex("value")).trim().equals("")){
                config.put("value", "");
            }
            else
                config.put("value", new JSONObject(c.getString(c.getColumnIndex("value"))));
        }
        config.put("etag", c.getString(c.getColumnIndex("etag")));
        config.put("key", c.getString(c.getColumnIndex("key")));
        c.close();
        return String.valueOf(config);

    }
}
