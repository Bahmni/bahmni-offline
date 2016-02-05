package org.bahmni.offline;

import android.content.Context;
import android.database.Cursor;

import org.bahmni.offline.db.AddressHierarchyService;
import org.bahmni.offline.db.AddressService;
import org.bahmni.offline.db.AttributeService;
import org.bahmni.offline.db.PatientService;

import net.danlew.android.joda.JodaTimeAndroid;
import net.sqlcipher.database.SQLiteDatabase;

import org.bahmni.offline.db.DbHelper;
import org.bahmni.offline.db.MarkerService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.JavascriptInterface;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class OfflineService {
    Context mContext;
    private DbHelper mDBHelper;
    private PatientService patientService;
    private AddressService addressService;
    private AttributeService attributeService;
    private MarkerService markerService;
    private AddressHierarchyService addressHierarchyService;


    OfflineService(Context c) {
        mContext = c;
        mDBHelper = new DbHelper(c, c.getExternalFilesDir(null) + "/Bahmni.db");
        JodaTimeAndroid.init(c);
        SQLiteDatabase.loadLibs(mContext);
        patientService = new PatientService(mDBHelper);
        addressService = new AddressService();
        attributeService = new AttributeService(new Util());
        markerService = new MarkerService(mDBHelper);
        addressHierarchyService = new AddressHierarchyService(mDBHelper);
    }

    OfflineService(Context c, AttributeService attributeServiceInjected) {
        mContext = c;
        mDBHelper = new DbHelper(c, c.getExternalFilesDir(null) + "/Bahmni.db");
        JodaTimeAndroid.init(c);
        SQLiteDatabase.loadLibs(mContext);
        patientService = new PatientService(mDBHelper);
        addressService = new AddressService();
        attributeService = attributeServiceInjected;
        markerService = new MarkerService(mDBHelper);
        addressHierarchyService = new AddressHierarchyService(mDBHelper);
    }

    @JavascriptInterface
    public void populateData(String host) throws IOException, JSONException {
//        TODO: Hemanth/Abishek/Ranganathan - We don't need to take host as a parameter once we build event log for attributeTypes.
        initSchema();
        SQLiteDatabase db = mDBHelper.getWritableDatabase(Constants.KEY);
//        TODO: Hemanth/Abishek/Ranganathan - Next line will go away once we build event log for attributeTypes
        attributeService.insertAttributeTypes(host, db);
    }

    @JavascriptInterface
    public String getPatientByUuid(String uuid) throws JSONException {
        return String.valueOf(patientService.getPatientByUuid(uuid));
    }

    @JavascriptInterface
    public String search(String sqlString) throws JSONException, IOException, ExecutionException, InterruptedException {
        JSONArray json = new OfflineSearch(mContext, mDBHelper).execute(sqlString).get();
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
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);

        insertPatientData(db, new JSONObject(request), requestType);

        String uuid = new JSONObject(request).getJSONObject("patient").getString("uuid");
        return String.valueOf(new JSONObject().put("data", new JSONObject(getPatientByUuid(uuid))));
    }

    @JavascriptInterface
    public String generateOfflineIdentifier() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("data", "TMP-" + patientService.generateIdentifier());
        return String.valueOf(result);
    }

    @JavascriptInterface
    public String insertMarker(String eventUuid, String catchmentNumber) {
        return markerService.insertMarker(eventUuid, catchmentNumber);
    }

    @JavascriptInterface
    public String getMarker() throws JSONException {
        JSONObject marker = markerService.getMarker();
        return marker == null ? null : String.valueOf(marker);
    }

    @JavascriptInterface
    public String insertAddressHierarchy(String addressHierarchy) throws JSONException {
        JSONObject addressHierarcyRequest = new JSONObject(addressHierarchy);
        JSONObject jsonObject = addressHierarchyService.insertAddressHierarchy(addressHierarcyRequest);
        return jsonObject == null ? null : String.valueOf(jsonObject);
    }


    private SQLiteDatabase initSchema() throws IOException, JSONException {
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("admin", "test".toCharArray());
            }
        });

        SQLiteDatabase db = mDBHelper.getWritableDatabase(Constants.KEY);

        mDBHelper.createTable(db, Constants.CREATE_PATIENT_TABLE);
        mDBHelper.createTable(db, Constants.CREATE_PATIENT_ATTRIBUTE_TYPE_TABLE);
        mDBHelper.createTable(db, Constants.CREATE_PATIENT_ATTRIBUTE_TABLE);
        mDBHelper.createTable(db, Constants.CREATE_EVENT_LOG_MARKER_TABLE);
        mDBHelper.createTable(db, Constants.CREATE_ADDRESS_HIERARCHY_ENTRY_TABLE);
        mDBHelper.createTable(db, Constants.CREATE_ADDRESS_HIERARCHY_LEVEL_TABLE);
        mDBHelper.createTable(db, Constants.CREATE_IDGEN_TABLE);
        mDBHelper.createTable(db, Constants.CREATE_PATIENT_ADDRESS_TABLE);

        mDBHelper.createIndex(db, Constants.CREATE_GIVEN_NAME_INDEX);
        mDBHelper.createIndex(db, Constants.CREATE_MIDDLE_NAME_INDEX);
        mDBHelper.createIndex(db, Constants.CREATE_FAMILY_NAME_INDEX);
        mDBHelper.createIndex(db, Constants.CREATE_IDENTIFIER_INDEX);

        return db;
    }

    private void insertPatientData(SQLiteDatabase db, JSONObject patientData, String requestType) throws JSONException {

        JSONObject person = patientData.getJSONObject("patient").getJSONObject("person");
        JSONArray attributes = person.getJSONArray("attributes");

        Cursor d = db.rawQuery("SELECT attributeTypeId, uuid, attributeName, format FROM patient_attribute_types", new String[]{});
        d.moveToFirst();
        ArrayList<JSONObject> attributeTypeMap = new ArrayList<JSONObject>();
        while (!d.isAfterLast()) {
            JSONObject attributeEntry = new JSONObject();
            attributeEntry.put("attributeTypeId", d.getInt(d.getColumnIndex("attributeTypeId")));
            attributeEntry.put("uuid", d.getString(d.getColumnIndex("uuid")));
            attributeEntry.put("attributeName", d.getString(d.getColumnIndex("attributeName")));
            attributeEntry.put("format", d.getString(d.getColumnIndex("format")));
            attributeTypeMap.add(attributeEntry);
            d.moveToNext();
        }
        d.close();
        if (requestType.equals("POST")) {
            parseAttributeValues(attributes, attributeTypeMap);
        }
        String patientUuid = patientService.insertPatient(db, patientData);


        attributeService.insertAttributes(db, patientUuid, attributes, attributeTypeMap);

        if (!person.isNull("preferredAddress")) {
            JSONObject address = person.getJSONObject("preferredAddress");
            addressService.insertAddress(db, address, patientUuid);
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
