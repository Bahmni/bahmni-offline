package org.bahmni.offline.services;

import android.content.Context;
import net.sqlcipher.database.SQLiteDatabase;

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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import static org.bahmni.offline.Constants.LOCATION_DB_VERSION;

public class DbService {
    private DbHelper locationDBHelper;
    private DbHelper metaDataDbHelper;
    private PatientDbService patientDbService;
    private PatientIdentifierDbService patientIdentifierDbService;
    private PatientAddressDbService patientAddressDbService;
    private PatientAttributeDbService patientAttributeDbService;
    private MarkerDbService markerDbService;
    private AddressHierarchyDbService addressHierarchyDbService;
    private EncounterDbService encounterDbService;
    private VisitDbService visitDbService;
    private ErrorLogDbService errorLogDbService;
    private ObservationDbService observationDbService;
    private LabOrderDbService labOrderDbService;
    private Context context;
    private ReferenceDataDbService referenceDataDbService;

    public DbService(Context context, DbHelper metaDataDBHelper) {
        this.context = context;
        this.metaDataDbHelper = metaDataDBHelper;
        this.referenceDataDbService = new ReferenceDataDbService(metaDataDBHelper);
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
    public String getPatientByUuid(String uuid) throws JSONException {
        return String.valueOf(patientDbService.getPatientByUuid(uuid));
    }

    @JavascriptInterface
    public String getEncountersByPatientUuid(String uuid) throws JSONException {
        return String.valueOf(encounterDbService.getEncountersByPatientUuid(uuid));
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
        JSONArray json = new SearchDbService(locationDBHelper).execute(params).get();
        return String.valueOf(new JSONObject().put("data", new JSONObject().put("pageOfResults", json)));
    }

    @JavascriptInterface
    public String insertObservationData(String patientUuid, String visitUuid, String observationData) throws JSONException {
        JSONArray jsonArray = observationDbService.insertObservationData(patientUuid, visitUuid, new JSONArray(observationData));
        return jsonArray == null ? null : String.valueOf(jsonArray);
    }

    @JavascriptInterface
    public void deletePatientData(String uuid) {
        SQLiteDatabase db = locationDBHelper.getReadableDatabase();

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

        JSONObject patientJson = new JSONObject(request).getJSONObject("patient");
        String uuid = patientJson.getString("uuid");
        boolean isVoided = !patientJson.isNull("voided") && patientJson.getBoolean("voided");
        if (!isVoided) {
            patientJson = new JSONObject(getPatientByUuid(uuid));
        }
        return String.valueOf(new JSONObject().put("data", patientJson));
    }

    @JavascriptInterface
    public String insertMarker(String markerName, String eventUuid, String filters)throws JSONException  {
        DbHelper dbHelper = markerName.equals("offline-concepts") ?  metaDataDbHelper : locationDBHelper;
        return markerDbService.insertMarker(dbHelper, markerName, eventUuid, filters);
    }

    @JavascriptInterface
    public String getMarker(String markerName) throws JSONException {
        DbHelper dbHelper = markerName.equals("offline-concepts") ?  metaDataDbHelper : locationDBHelper;
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
    public void init() throws IOException, JSONException {
        patientDbService = new PatientDbService(locationDBHelper);
        patientIdentifierDbService = new PatientIdentifierDbService(locationDBHelper);
        patientAddressDbService = new PatientAddressDbService(locationDBHelper);
        patientAttributeDbService = new PatientAttributeDbService(locationDBHelper);
        markerDbService = new MarkerDbService();
        addressHierarchyDbService = new AddressHierarchyDbService(locationDBHelper);
        encounterDbService = new EncounterDbService(locationDBHelper);
        visitDbService = new VisitDbService(locationDBHelper);
        errorLogDbService = new ErrorLogDbService(locationDBHelper);
        observationDbService = new ObservationDbService(locationDBHelper);
        labOrderDbService = new LabOrderDbService(locationDBHelper);
    }


    @JavascriptInterface
    public void initSchema(String DbName) throws IOException, JSONException {
        if(!DbName.equals("metaData")) {
            String databaseName = "/" + DbName + ".db";
            String dbPath = context.getExternalFilesDir(null) + databaseName;
            locationDBHelper = new DbHelper(context, dbPath, LOCATION_DB_VERSION);
        }
    }

    @JavascriptInterface
    public String insertEncounterData(String request) throws JSONException {
        JSONObject jsonObject = encounterDbService.insertEncounterData(new JSONObject(request));
        return jsonObject == null ? null : String.valueOf(jsonObject);
    }

    @JavascriptInterface
    public String insertVisitData(String request) throws JSONException {
        JSONObject jsonObject = visitDbService.insertVisitData(new JSONObject(request));
        return jsonObject == null ? null : String.valueOf(jsonObject);
    }

    @JavascriptInterface
    public String findActiveEncounter(String params, String encounterSessionDurationInMinutes) throws JSONException {
        JSONObject encounterData = encounterDbService.findActiveEncounter(new JSONObject(params), Integer.parseInt(encounterSessionDurationInMinutes));
        return encounterData == null ? null : String.valueOf(encounterData);
    }

    @JavascriptInterface
    public String getEncountersByVisits(String params) throws JSONException {
        JSONArray encounterData = encounterDbService.getEncountersByVisits(new JSONObject(params));
        return encounterData == null ? null : String.valueOf(encounterData);
    }

    private void insertPatientData(JSONObject patientData) throws JSONException {
        JSONObject person = patientData.getJSONObject("patient").getJSONObject("person");
        JSONArray patientIdentifiers = patientData.getJSONObject("patient").getJSONArray("identifiers");
        JSONArray attributes = person.getJSONArray("attributes");

//        ArrayList<JSONObject> attributeTypeMap = patientAttributeDbService.getAttributeTypes();
        String patientUuid = patientDbService.insertPatient(patientData);
        patientIdentifierDbService.insertPatientIdentifiers(patientUuid, patientIdentifiers);
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
    public String findEncounterByEncounterUuid(String encounterUuid) throws JSONException {
        JSONObject encounterData = encounterDbService.findEncounterByEncounterUuid(encounterUuid);
        return encounterData == null ? null : String.valueOf(encounterData);
    }

    @JavascriptInterface
    public void insertLog(String uuid,String failedRequest, int responseStatus, String stackTrace, String requestPayload, String provider) throws JSONException {
        errorLogDbService.insertLog(uuid, failedRequest, responseStatus, stackTrace, requestPayload, provider);
    }

    @JavascriptInterface
    public String getAllLogs() throws JSONException {
        return errorLogDbService.getAllLogs().toString();
    }

    @JavascriptInterface
    public void deleteByUuid(String uuid){
        errorLogDbService.deleteByUuid(uuid);
    }

    @JavascriptInterface
    public String getErrorLogByUuid(String uuid) throws JSONException{
        return String.valueOf(errorLogDbService.getErrorLogByUuid(uuid));
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
