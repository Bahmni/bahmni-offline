package org.bahmni.offline.services;

import android.content.Context;
import net.sqlcipher.database.SQLiteDatabase;

import net.sqlcipher.database.SQLiteException;
import org.bahmni.offline.dbServices.dao.AddressHierarchyDbService;
import org.bahmni.offline.dbServices.dao.DbHelper;
import org.bahmni.offline.dbServices.dao.EncounterDbService;
import org.bahmni.offline.dbServices.dao.ErrorLogDbService;
import org.bahmni.offline.dbServices.dao.LabOrderDbService;
import org.bahmni.offline.dbServices.dao.MarkerDbService;
import org.bahmni.offline.dbServices.dao.ObservationDbService;
import org.bahmni.offline.dbServices.dao.PatientAddressDbService;
import org.bahmni.offline.dbServices.dao.PatientAttributeDbService;
import org.bahmni.offline.dbServices.dao.PatientDbService;
import org.bahmni.offline.dbServices.dao.PatientIdentifierDbService;
import org.bahmni.offline.dbServices.dao.ReferenceDataDbService;
import org.bahmni.offline.dbServices.dao.SearchDbService;
import org.bahmni.offline.dbServices.dao.VisitDbService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.JavascriptInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import static org.bahmni.offline.Constants.LOCATION_DB_VERSION;

public class DbService {
    private DbHelper appDBHelper;
    private DbHelper metaDataDbHelper;
    private PatientIdentifierDbService patientIdentifierDbService;
    private PatientAddressDbService patientAddressDbService;
    private PatientAttributeDbService patientAttributeDbService;
    private MarkerDbService markerDbService;
    private AddressHierarchyDbService addressHierarchyDbService;
    private VisitDbService visitDbService;
    private ObservationDbService observationDbService;
    private LabOrderDbService labOrderDbService;
    private Context context;
    private ReferenceDataDbService referenceDataDbService;
    private EncounterDbService encounterDbService = new EncounterDbService();
    private ErrorLogDbService errorLogDbService = new ErrorLogDbService();
    private PatientDbService patientDbService = new PatientDbService();
    private HashMap<String, DbHelper> dbHelpers = new HashMap<String, DbHelper>();

    public DbService(Context context, DbHelper metaDataDBHelper) {
        this.context = context;
        this.metaDataDbHelper = metaDataDBHelper;
        this.referenceDataDbService = new ReferenceDataDbService(metaDataDBHelper);
    }

    private DbHelper getDbHelper(String dbName) {
        return dbName != null ? dbHelpers.get(dbName) : null;
    }

    @JavascriptInterface
    public String getLog(){
       try {
           Process process = Runtime.getRuntime().exec("logcat -d -v brief -t 100");
           BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

           StringBuilder log = new StringBuilder();
           String line;
           while ((line = bufferedReader.readLine()) != null) {
               log.append(line);
           }
           return String.valueOf(log);
       }
       catch (IOException e) {
           return "Error getting logs";
       }
    }


    @JavascriptInterface
    public String getPatientByUuid(String uuid, String dbName) throws JSONException {
        if (dbName != null) {
            return String.valueOf(patientDbService.getPatientByUuid(uuid, dbHelpers.get(dbName)));
        }
        return String.valueOf(patientDbService.getPatientByUuid(uuid, appDBHelper));
    }

    @JavascriptInterface
    public String getEncountersByPatientUuid(String uuid) throws JSONException {
        return String.valueOf(encounterDbService.getEncountersByPatientUuid(uuid, appDBHelper));
    }

    @JavascriptInterface
    public String getVisitByUuid(String uuid) throws JSONException {
        return String.valueOf(visitDbService.getVisitByUuid(uuid));
    }

    @JavascriptInterface
    public String getVisitsByPatientUuid(String patientUuid, int numberOfVisits) throws JSONException {
        JSONArray jsonArray = visitDbService.getVisitsByPatientUuid(patientUuid, numberOfVisits);
        return jsonArray == null ? null : String.valueOf(jsonArray);
    }

    @JavascriptInterface
    public String getAttributeTypes() throws JSONException {
        return String.valueOf(patientAttributeDbService.getAttributeTypes());
    }

    @JavascriptInterface
    public String getObservationsFor(String params) throws JSONException {
        JSONArray jsonArray = observationDbService.getObservationsFor(new JSONObject(params));
        return jsonArray == null ? null : String.valueOf(jsonArray);
    }

    @JavascriptInterface
    public String search(String params) throws JSONException, IOException, ExecutionException, InterruptedException {
        JSONArray json = new SearchDbService(appDBHelper).execute(params).get();
        return String.valueOf(new JSONObject().put("data", new JSONObject().put("pageOfResults", json)));
    }

    @JavascriptInterface
    public String insertObservationData(String patientUuid, String visitUuid, String observationData, String dbName) throws JSONException {
        DbHelper dbHelper = getDbHelper(dbName);
        JSONArray jsonArray = observationDbService.insertObservationData(patientUuid, visitUuid, new JSONArray(observationData), dbHelper);
        return jsonArray == null ? null : String.valueOf(jsonArray);
    }

    @JavascriptInterface
    public void deleteByEncounterUuid(String encounterUuid, String dbName){
        observationDbService.deleteByEncounterUuid(getDbHelper(dbName), encounterUuid);
    }

    @JavascriptInterface
    public void deletePatientData(String uuid) {
        SQLiteDatabase db = appDBHelper.getReadableDatabase();

        db.beginTransaction();

        db.rawExecSQL("DELETE FROM patient_attributes WHERE patientUuid = '" + uuid + "'");
        db.rawExecSQL("DELETE FROM patient_address WHERE patientUuid = '" + uuid + "'");
        db.rawExecSQL("DELETE FROM patient WHERE uuid = '" + uuid + "'");

        db.setTransactionSuccessful();

        db.endTransaction();
    }

    @JavascriptInterface
    public String createPatient(String request) throws JSONException, IOException, ExecutionException, InterruptedException {
        JSONObject patientJson = new JSONObject(request).getJSONObject("patient");
        try {
            insertPatientData(new JSONObject(request));
        } catch (SQLiteException e) {
            JSONObject errorMessage = new JSONObject();
            errorMessage.put("message", "Patient failed to validate with reason: Identifier "+ e.getMessage() +" is already in use by another patient");
            errorMessage.put("isIdentifierDuplicate", true);
            return String.valueOf(errorMessage);
        }

        String uuid = patientJson.getString("uuid");
        boolean isVoided = !patientJson.isNull("voided") && patientJson.getBoolean("voided");
        if (!isVoided) {
            patientJson = new JSONObject(getPatientByUuid(uuid, null));
        }
        return String.valueOf(new JSONObject().put("data", patientJson));
    }

    @JavascriptInterface
    public String insertMarker(String markerName, String eventUuid, String filters)throws JSONException  {
        DbHelper dbHelper = isMetaData(markerName) ?  metaDataDbHelper : appDBHelper;
        return markerDbService.insertMarker(dbHelper, markerName, eventUuid, filters);
    }

    private boolean isMetaData(String markerName) {
        return markerName.equals("offline-concepts") || markerName.equals("forms");
    }

    @JavascriptInterface
    public String getMarker(String markerName) throws JSONException {
        DbHelper dbHelper = isMetaData(markerName) ?  metaDataDbHelper : appDBHelper;
        JSONObject marker = markerDbService.getMarker(dbHelper, markerName);
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
    public void init(String dbName) throws IOException, JSONException {
        appDBHelper = dbHelpers.get(dbName);
        patientIdentifierDbService = new PatientIdentifierDbService(appDBHelper);
        patientAddressDbService = new PatientAddressDbService(appDBHelper);
        patientAttributeDbService = new PatientAttributeDbService(appDBHelper);
        markerDbService = new MarkerDbService();
        addressHierarchyDbService = new AddressHierarchyDbService(appDBHelper);
        visitDbService = new VisitDbService(appDBHelper);
        observationDbService = new ObservationDbService(appDBHelper);
        labOrderDbService = new LabOrderDbService(appDBHelper);
    }


    @JavascriptInterface
    public String initSchema(String dbName) throws IOException, JSONException {
        if(!dbName.equals("metaData")) {
            String databaseName = "/" + dbName + ".db";
            String dbPath = context.getExternalFilesDir(null) + databaseName;
            dbHelpers.put(dbName, new DbHelper(context, dbPath, LOCATION_DB_VERSION));
        }
        return dbName;
    }

    @JavascriptInterface
    public String insertEncounterData(String request, String dbName) throws JSONException {
        DbHelper dbHelper = dbName != null ? dbHelpers.get(dbName) : appDBHelper;
        JSONObject jsonObject = encounterDbService.insertEncounterData(new JSONObject(request), dbHelper);
        return jsonObject == null ? null : String.valueOf(jsonObject);
    }

    @JavascriptInterface
    public String insertVisitData(String request, String dbName) throws JSONException {
        DbHelper dbHelper = getDbHelper(dbName);
        JSONObject jsonObject = visitDbService.insertVisitData(new JSONObject(request), dbHelper);
        return jsonObject == null ? null : String.valueOf(jsonObject);
    }

    @JavascriptInterface
    public String findActiveEncounter(String params, String encounterSessionDurationInMinutes) throws JSONException {
        JSONObject encounterData = encounterDbService.findActiveEncounter(new JSONObject(params), Integer.parseInt(encounterSessionDurationInMinutes), appDBHelper);
        return encounterData == null ? null : String.valueOf(encounterData);
    }

    @JavascriptInterface
    public String getEncountersByVisits(String params) throws JSONException {
        JSONArray encounterData = encounterDbService.getEncountersByVisits(new JSONObject(params), appDBHelper);
        return encounterData == null ? null : String.valueOf(encounterData);
    }

    private void insertPatientData(JSONObject patientData) throws JSONException, SQLiteException {
        JSONObject person = patientData.getJSONObject("patient").getJSONObject("person");
        JSONArray patientIdentifiers = patientData.getJSONObject("patient").getJSONArray("identifiers");
        JSONArray attributes = person.getJSONArray("attributes");
        String patientUuid = person.getString("uuid");

        patientIdentifierDbService.insertPatientIdentifiers(patientUuid, patientIdentifiers);
        patientDbService.insertPatient(patientData, appDBHelper);
        patientAttributeDbService.insertAttributes(patientUuid, attributes);

        JSONObject address;
        if (!person.isNull("addresses") && person.getJSONArray("addresses").length() > 0) {
            address = person.getJSONArray("addresses").getJSONObject(0);
            patientAddressDbService.insertAddress(address, patientUuid);
        } else if (!person.isNull("preferredAddress")){
            address = person.getJSONObject("preferredAddress");
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

    @JavascriptInterface
    public String findEncounterByEncounterUuid(String encounterUuid, String dbName) throws JSONException {
        JSONObject encounterData;
        DbHelper dbHelper = dbName != null ? dbHelpers.get(dbName) : appDBHelper;
        encounterData = encounterDbService.findEncounterByEncounterUuid(encounterUuid, dbHelper);
        return encounterData == null ? null : String.valueOf(encounterData);
    }

    @JavascriptInterface
    public void insertLog(String uuid,String failedRequest, int responseStatus, String stackTrace, String requestPayload, String provider) throws JSONException {
        errorLogDbService.insertLog(uuid, failedRequest, responseStatus, stackTrace, requestPayload, provider, appDBHelper);
    }

    @JavascriptInterface
    public String getAllLogs() throws JSONException {
        return errorLogDbService.getAllLogs(appDBHelper).toString();
    }

    @JavascriptInterface
    public void deleteByUuid(String uuid){
        errorLogDbService.deleteByUuid(uuid, appDBHelper);
    }

    @JavascriptInterface
    public String getErrorLogByUuid(String uuid, String dbName) throws JSONException{
        DbHelper dbHelper = dbName != null ? dbHelpers.get(dbName) : appDBHelper;
        return String.valueOf(errorLogDbService.getErrorLogByUuid(uuid, dbHelper));
    }

    @JavascriptInterface
    public String getVisitDetailsByPatientUuid(String uuid) throws JSONException {
        return String.valueOf(visitDbService.getVisitDetailsByPatientUuid(uuid));
    }

    @JavascriptInterface
    public String getObservationsForVisit(String visitUuid) throws JSONException {
        return String.valueOf(observationDbService.getObservationsForVisit(visitUuid));
    }

    @JavascriptInterface
    public String insertLabOrderResults(String patientUuid, String labOrderResults) throws JSONException {
        return labOrderDbService.insertLabOrderResults(patientUuid, labOrderResults);
    }

    @JavascriptInterface
    public String getLabOrderResultsByPatientUuid(String patientUuid) throws JSONException {
        return String.valueOf(labOrderDbService.getLabOrderResultsByPatientUuid(patientUuid));
    }

    @JavascriptInterface
    public void insertReferenceData(String referenceDataKey, String data, String eTag) throws JSONException {
        if(referenceDataKey.equals("PersonAttributeType")){
            referenceDataDbService.insertReferenceData(referenceDataKey, data ,eTag);
            patientAttributeDbService.insertAttributeTypes(String.valueOf(new JSONObject(data).getJSONArray("results")));
        }
        else {
            referenceDataDbService.insertReferenceData(referenceDataKey, data ,eTag);
        }
    }

    @JavascriptInterface
    public String getReferenceData(String referenceDataKey) throws JSONException {
        return  referenceDataDbService.getReferenceData(referenceDataKey);
    }
}
