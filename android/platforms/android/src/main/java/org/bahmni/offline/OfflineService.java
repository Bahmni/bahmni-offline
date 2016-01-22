package org.bahmni.offline;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

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
import java.net.URL;
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
        attributeService = new AttributeService();
        markerService = new MarkerService(mDBHelper);
        addressHierarchyService = new AddressHierarchyService(mDBHelper);
    }

    @JavascriptInterface
    public void init(JSONObject offlineDbObjectForLoveField) {
        // Hemanth: This method should exist.
    }

    @JavascriptInterface
    public void populateData(String host) throws IOException, JSONException {
            initSchema(host);
            SQLiteDatabase db = mDBHelper.getWritableDatabase(Constants.KEY);

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
    public void deletePatientData(String patientIdentifier) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
        Cursor c = db.rawQuery("SELECT _id from patient" +
                " WHERE identifier = '" + patientIdentifier + "' limit 1 ", new String[]{});
        c.moveToFirst();
        int patientId = c.getInt(c.getColumnIndex("_id"));

        db.beginTransaction();

        db.rawExecSQL("DELETE FROM patient_attributes WHERE patientId = '" + patientId + "'");
        db.rawExecSQL("DELETE FROM patient_address WHERE patientId = '" + patientId + "'");
        db.rawExecSQL("DELETE FROM patient WHERE _id = '" + patientId + "'");

        db.setTransactionSuccessful();

        db.endTransaction();
        c.close();
    }

    @JavascriptInterface
    public String getPatientByIdentifier(String identifier) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("data", new JSONObject().put("pageOfResults", new JSONArray().put(patientService.getPatientByIdentifier(identifier))));
        return String.valueOf(result);
    }

    @JavascriptInterface
    public String createPatient(String request, String host) throws JSONException, IOException, ExecutionException, InterruptedException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);

        insertPatientData(db, request, Util.getAddressColumns(host), "POST");

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
    public JSONObject getMarker() throws JSONException {
        return markerService.getMarker();
    }

    @JavascriptInterface
    public JSONObject insertAddressHierarchy(JSONObject addressHierarchy) throws JSONException {
        return addressHierarchyService.insertAddressHierarchy(addressHierarchy);
    }


    private void createIndices(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX IF NOT EXISTS givenNameIndex ON patient(givenName)");
        db.execSQL("CREATE INDEX IF NOT EXISTS middleNameIndex ON patient(middleName)");
        db.execSQL("CREATE INDEX IF NOT EXISTS familyNameIndex ON patient(familyName)");
        db.execSQL("CREATE INDEX IF NOT EXISTS identifierIndex ON patient(identifier)");
    }

    private SQLiteDatabase initSchema(String host) throws IOException, JSONException {
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("admin", "test".toCharArray());
            }
        });

        SQLiteDatabase db = mDBHelper.getWritableDatabase(Constants.KEY);

        mDBHelper.createTableBy(db, Constants.CREATE_PATIENT_TABLE);
        mDBHelper.createTableBy(db, Constants.CREATE_PATIENT_ATTRIBUTE_TYPE_TABLE);
        mDBHelper.createTableBy(db, Constants.CREATE_PATIENT_ATTRIBUTE_TABLE);
        mDBHelper.createTableBy(db, Constants.CREATE_EVENT_LOG_MARKER_TABLE);
        mDBHelper.createTableBy(db, Constants.CREATE_ADDRESS_HIERARCHY_ENTRY_TABLE);
        mDBHelper.createTableBy(db, Constants.CREATE_ADDRESS_HIERARCHY_LEVEL_TABLE);
        mDBHelper.createTableBy(db, Constants.CREATE_IDGEN_TABLE);
        String[] addressColumnNames = Util.getAddressColumns(host);
        mDBHelper.createTable(db, "patient_address", addressColumnNames);
        createIndices(db);

        return db;
    }

    private void insertPatientData(SQLiteDatabase db, String patientObject, String[] addressColumnNames, String requestType) throws JSONException {

        String patientUuid = patientService.insertPatient(db, patientObject);

        JSONObject person = new JSONObject(patientObject).getJSONObject("patient").getJSONObject("person");
        JSONArray attributes = person.getJSONArray("attributes");

        attributeService.insertAttributes(db, patientUuid, attributes, requestType);

        if (!person.isNull("preferredAddress")) {
            JSONObject address = person.getJSONObject("preferredAddress");
            addressService.insertAddress(db, address, addressColumnNames, patientUuid);
        }
    }
}
