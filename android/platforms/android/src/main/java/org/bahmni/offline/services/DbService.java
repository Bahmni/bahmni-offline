package org.bahmni.offline.services;

import android.content.ContentValues;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.bahmni.offline.dbServices.dao.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.JavascriptInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class DbService {
    private DbHelper mDBHelper;
    private PatientDbService patientDbService;
    private PatientAddressDbService patientAddressDbService;
    private PatientAttributeDbService patientAttributeDbService;
    private MarkerDbService markerDbService;
    private AddressHierarchyDbService addressHierarchyDbService;
    private EncounterDbService encounterDbService;
    private VisitDbService visitDbService;
    private ErrorLogDbService errorLogDbService;
    private ObservationDbService observationDbService;



    public DbService(DbHelper mDBHelper) {
        this.mDBHelper = mDBHelper;
        patientDbService = new PatientDbService(mDBHelper);
        patientAddressDbService = new PatientAddressDbService(mDBHelper);
        patientAttributeDbService = new PatientAttributeDbService(mDBHelper);
        markerDbService = new MarkerDbService(mDBHelper);
        addressHierarchyDbService = new AddressHierarchyDbService(mDBHelper);
        encounterDbService = new EncounterDbService(mDBHelper);
        visitDbService = new VisitDbService(mDBHelper);
        errorLogDbService = new ErrorLogDbService(mDBHelper);
        observationDbService = new ObservationDbService(mDBHelper);
    }
    
    @JavascriptInterface
    public String getLog(){
       try {
           Process process = Runtime.getRuntime().exec("logcat -d -v brief -t 100");
           BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

           StringBuilder log = new StringBuilder();
           String line = "";
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
        JSONArray json = new SearchDbService(mDBHelper).execute(params).get();
        return String.valueOf(new JSONObject().put("data", new JSONObject().put("pageOfResults", json)));
    }

    @JavascriptInterface
    public String insertObservationData(String patientUuid, String visitUuid, String observationData) throws JSONException {
        JSONArray jsonArray = observationDbService.insertObservationData(patientUuid, visitUuid, new JSONArray(observationData));
        return jsonArray == null ? null : String.valueOf(jsonArray);
    }

    @JavascriptInterface
    public void deletePatientData(String uuid) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();

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
    public String insertMarker(String markerName, String eventUuid, String catchmentNumber)throws JSONException  {
        return markerDbService.insertMarker(markerName, eventUuid, catchmentNumber);
    }

    @JavascriptInterface
    public String getMarker(String markerName) throws JSONException {
        JSONObject marker = markerDbService.getMarker(markerName);
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
}
