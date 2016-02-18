package org.bahmni.offline.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.bahmni.offline.AddressHierarchyEntry;
import org.bahmni.offline.Constants;
import org.bahmni.offline.Util;
import org.bahmni.offline.dbServices.dao.*;

import net.danlew.android.joda.JodaTimeAndroid;
import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.JavascriptInterface;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    public void populateData(String params) throws IOException, JSONException {
//        TODO: Hemanth/Abishek/Ranganathan - We don't need to take host as a parameter once we build event log for attributeTypes.
//        initSchema();
//        TODO: Hemanth/Abishek/Ranganathan - Next line will go away once we build event log for attributeTypes
        patientAttributeDbService.insertAttributeTypes(params);
    }

    @JavascriptInterface
    public String getPatientByUuid(String uuid) throws JSONException {
        return String.valueOf(patientDbService.getPatientByUuid(uuid));
    }

    @JavascriptInterface
    public String search(String params) throws JSONException, IOException, ExecutionException, InterruptedException {
        JSONArray json = new SearchDbService(mDBHelper).execute(params).get();
        return String.valueOf(new JSONObject().put("pageOfResults", json));
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
    public String createPatient(String request, String requestType) throws JSONException, IOException, ExecutionException, InterruptedException {

        insertPatientData(new JSONObject(request), requestType);

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

        mDBHelper.createTable(Constants.CREATE_PATIENT_TABLE);
        mDBHelper.createTable(Constants.CREATE_PATIENT_ATTRIBUTE_TYPE_TABLE);
        mDBHelper.createTable(Constants.CREATE_PATIENT_ATTRIBUTE_TABLE);
        mDBHelper.createTable(Constants.CREATE_EVENT_LOG_MARKER_TABLE);
        mDBHelper.createTable(Constants.CREATE_ADDRESS_HIERARCHY_ENTRY_TABLE);
        mDBHelper.createTable(Constants.CREATE_ADDRESS_HIERARCHY_LEVEL_TABLE);
        mDBHelper.createTable(Constants.CREATE_IDGEN_TABLE);
        mDBHelper.createTable(Constants.CREATE_PATIENT_ADDRESS_TABLE);
        mDBHelper.createTable(Constants.CREATE_CONFIG_TABLE);

        mDBHelper.createIndex(Constants.CREATE_GIVEN_NAME_INDEX);
        mDBHelper.createIndex(Constants.CREATE_MIDDLE_NAME_INDEX);
        mDBHelper.createIndex(Constants.CREATE_FAMILY_NAME_INDEX);
        mDBHelper.createIndex(Constants.CREATE_IDENTIFIER_INDEX);

    }

    private void insertPatientData(JSONObject patientData, String requestType) throws JSONException {
        JSONObject person = patientData.getJSONObject("patient").getJSONObject("person");
        JSONArray attributes = person.getJSONArray("attributes");

        ArrayList<JSONObject> attributeTypeMap = patientAttributeDbService.getAttributeTypes();
        if (requestType.equals("POST")) {
            parseAttributeValues(attributes, attributeTypeMap);
        }
        String patientUuid = patientDbService.insertPatient(patientData);


        patientAttributeDbService.insertAttributes(patientUuid, attributes, attributeTypeMap);

        if (!person.isNull("preferredAddress")) {
            JSONObject address = person.getJSONObject("preferredAddress");
            patientAddressDbService.insertAddress(address, patientUuid);
        }
    }

    private void parseAttributeValues(JSONArray attributes, ArrayList<JSONObject> attributeTypeMap) throws JSONException {
        for (int i = 0; i < attributes.length(); i++) {
            JSONObject attribute = attributes.getJSONObject(i);
            if (attribute.isNull("voided") || (!attribute.isNull("voided") && !attribute.getBoolean("voided"))) {
                String format = getFormat(attributeTypeMap, attribute);
                if ("java.lang.Integer".equals(format)) {
                    attribute.put("value", Integer.parseInt(attribute.getString("value")));
                }
                if ("java.lang.Float".equals(format)) {
                    attribute.put("value", Float.parseFloat(attribute.getString("value")));
                } else if ("java.lang.Boolean".equals(format)) {
                    attribute.put("value", attribute.getString("value").equals("true"));

                } else if ("org.openmrs.Concept".equals(format)) {
                    String display = attribute.getString("value");
                    JSONObject value = new JSONObject();
                    value.put("display", display);
                    value.put("uuid", attribute.getString("hydratedObject"));
                    attribute.put("value", value);
                }
            }
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
