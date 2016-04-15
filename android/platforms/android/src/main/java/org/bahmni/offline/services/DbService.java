package org.bahmni.offline.services;

import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.bahmni.offline.dbServices.dao.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.JavascriptInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class DbService {
    private DbHelper mDBHelper;
    private PatientDbService patientDbService;
    private PatientAddressDbService patientAddressDbService;
    private PatientAttributeDbService patientAttributeDbService;
    private MarkerDbService markerDbService;
    private AddressHierarchyDbService addressHierarchyDbService;


    public DbService(DbHelper mDBHelper) {
        this.mDBHelper = mDBHelper;
        patientDbService = new PatientDbService(mDBHelper);
        patientAddressDbService = new PatientAddressDbService(mDBHelper);
        patientAttributeDbService = new PatientAttributeDbService(mDBHelper);
        markerDbService = new MarkerDbService(mDBHelper);
        addressHierarchyDbService = new AddressHierarchyDbService(mDBHelper);
    }

    @JavascriptInterface
    public String getPatientByUuid(String uuid) throws JSONException {
        return String.valueOf(patientDbService.getPatientByUuid(uuid));
    }

    @JavascriptInterface
    public String getAttributeTypes() throws JSONException {
        return String.valueOf(patientAttributeDbService.getAttributeTypes());
    }

    @JavascriptInterface
    public String search(String params) throws JSONException, IOException, ExecutionException, InterruptedException {
        JSONArray json = new SearchDbService(mDBHelper).execute(params).get();
        return String.valueOf(new JSONObject().put("data", new JSONObject().put("pageOfResults", json)));
    }

    @JavascriptInterface
    public void deletePatientData(String uuid) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);

        db.beginTransaction();

        db.rawExecSQL("DELETE FROM patient_attributes WHERE patientUuid = '" + uuid + "'");
        db.rawExecSQL("DELETE FROM patient_address WHERE patientUuid = '" + uuid + "'");
        db.rawExecSQL("DELETE FROM patient WHERE uuid = '" + uuid + "'");

        db.setTransactionSuccessful();

        db.endTransaction();
    }

    @JavascriptInterface
    public String createPatient(String request) throws JSONException, IOException, ExecutionException, InterruptedException {

        insertPatientData(new JSONObject(request));

        String uuid = new JSONObject(request).getJSONObject("patient").getString("uuid");
        return String.valueOf(new JSONObject().put("data", new JSONObject(getPatientByUuid(uuid))));
    }

    @JavascriptInterface
    public String insertMarker(String eventUuid, String catchmentNumber) {
        return markerDbService.insertMarker(eventUuid, catchmentNumber);
    }

    @JavascriptInterface
    public String getMarker() throws JSONException {
        JSONObject marker = markerDbService.getMarker();
        return marker == null ? null : String.valueOf(marker);
    }

    @JavascriptInterface
    public String insertAddressHierarchy(String addressHierarchy) throws JSONException {
        JSONObject addressHierarcyRequest = new JSONObject(addressHierarchy);
        JSONObject jsonObject = addressHierarchyDbService.insertAddressHierarchy(addressHierarcyRequest);
        return jsonObject == null ? null : String.valueOf(jsonObject);
    }

    @JavascriptInterface
    public String searchAddress(String addressHierarchy) throws JSONException {
        JSONObject addressHierarchyRequest = new JSONObject(addressHierarchy);
        JSONArray addressAsJSONArray = addressHierarchyDbService.search(addressHierarchyRequest);
        return addressAsJSONArray == null ? null : addressAsJSONArray.toString();
    }


    @JavascriptInterface
    public void initSchema() throws IOException, JSONException {
        mDBHelper.runMigration(mDBHelper.getWritableDatabase(Constants.KEY), "migration_0.sql");

    }

    private void insertPatientData(JSONObject patientData) throws JSONException {
        JSONObject person = patientData.getJSONObject("patient").getJSONObject("person");
        JSONArray attributes = person.getJSONArray("attributes");

//        ArrayList<JSONObject> attributeTypeMap = patientAttributeDbService.getAttributeTypes();
        String patientUuid = patientDbService.insertPatient(patientData);


        patientAttributeDbService.insertAttributes(patientUuid, attributes);

        if (!person.isNull("addresses")) {
            JSONObject address = person.getJSONArray("addresses").getJSONObject(0);
            patientAddressDbService.insertAddress(address, patientUuid);
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
