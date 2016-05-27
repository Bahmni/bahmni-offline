package org.bahmni.offline.dbServices.dao;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.bahmni.offline.MainActivity;
import org.bahmni.offline.Utils.TestUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;


public class SearchDbServiceTest extends ActivityInstrumentationTestCase2<MainActivity>{

    public SearchDbServiceTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        super(MainActivity.class);
    }

    private DbHelper mDBHelper;
    private JSONObject patientData;
    PatientDbService patientDbService;

    public void setUp() throws Exception {
        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);
        mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_PATIENT_TABLE);
        mDBHelper.createTable(Constants.CREATE_PATIENT_ADDRESS_TABLE);
        mDBHelper.createTable(Constants.CREATE_PATIENT_ATTRIBUTE_TYPE_TABLE);
        mDBHelper.createTable(Constants.CREATE_PATIENT_ATTRIBUTE_TABLE);
        mDBHelper.createTable(Constants.CREATE_ENCOUNTER_TABLE);


        patientDbService = new PatientDbService(mDBHelper);
        PatientAttributeDbService patientAttributeDbService = new PatientAttributeDbService(mDBHelper);
        PatientAddressDbService patientAddressDbService = new PatientAddressDbService(mDBHelper);

        String uuid = "e34992ca-894f-4344-b4b3-54a4aa1e5558";
        String patientJson = TestUtils.readFileFromAssets("patient.json", getInstrumentation().getContext());
        patientData = new JSONObject(patientJson);
        patientDbService.insertPatient(patientData);

        JSONObject person = patientData.getJSONObject("patient").getJSONObject("person");
        JSONArray attributes = person.getJSONArray("attributes");
        JSONObject address = person.getJSONObject("preferredAddress");
        patientAddressDbService.insertAddress(address, uuid);


        String patientAttributeTypesJSON = TestUtils.readFileFromAssets("patientAttributeTypes.json", getInstrumentation().getContext());
        JSONObject patientAttributeTypes = new JSONObject(patientAttributeTypesJSON);
        patientAttributeDbService.insertAttributeTypes(patientAttributeTypes.get("results").toString());
        patientAttributeDbService.insertAttributes(uuid, attributes);
    }

    private void executeSearch(final JSONObject params, final JSONArray[] returnValue) throws Throwable {
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    returnValue[0] = new SearchDbService(mDBHelper).execute(String.valueOf(params)).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }



    @Test
    public void testShouldSearchByFirstName() throws Throwable {

        String searchString = "test";

        final JSONObject params = new JSONObject();
        params.put("q", searchString);
        params.put("s", "byIdOrNameOrVillage");
        params.put("startIndex", 0);
        params.put("addressFieldName", "address2");

        final JSONArray[] returnValue = new JSONArray[1];

        executeSearch(params, returnValue);

        JSONObject result = returnValue[0].getJSONObject(0);
        assertEquals("test", result.getString("givenName"));
    }

    @Test
    public void testShouldSearchByLastName() throws Throwable {

        String searchString = "integration";

        final JSONObject params = new JSONObject();
        params.put("q", searchString);
        params.put("s", "byIdOrNameOrVillage");
        params.put("startIndex", 0);
        params.put("addressFieldName", "address2");

        final JSONArray[] returnValue = new JSONArray[1];

        executeSearch(params, returnValue);

        JSONObject result = returnValue[0].getJSONObject(0);
        assertEquals("integration", result.getString("familyName"));
    }

    @Test
    public void testShouldSearchByAddress() throws Throwable {

        String searchString = "Chattisgarh";

        final JSONObject params = new JSONObject();
        params.put("q", "");
        params.put("s", "byIdOrNameOrVillage");
        params.put("startIndex", 0);
        params.put("addressFieldName", "stateProvince");
        params.put("addressFieldValue", searchString);

        final JSONArray[] returnValue = new JSONArray[1];
        JSONObject addressFieldValue = new JSONObject();
        addressFieldValue.put("stateProvince",searchString );

        executeSearch(params, returnValue);

        JSONObject result = returnValue[0].getJSONObject(0);
        assertEquals(addressFieldValue.toString(), result.getString("addressFieldValue"));
    }


    @Test
    public void testShouldSearchByIdentifier() throws Throwable {

        String searchString = "GAN200076";

        final JSONObject params = new JSONObject();
        params.put("q", searchString);
        params.put("s", "byIdOrNameOrVillage");
        params.put("startIndex", 0);
        params.put("addressFieldName", "address2");

        final JSONArray[] returnValue = new JSONArray[1];

        executeSearch(params, returnValue);

        JSONObject result = returnValue[0].getJSONObject(0);
        assertEquals("GAN200076", result.getString("identifier"));
    }

    @Test
    public void testShouldSearchByAttributes() throws Throwable {

        JSONArray searchArray = new JSONArray().put("caste").put("isUrban").put("education").put("landHolding");
        String searchString = "hindu";

        final JSONObject params = new JSONObject();
        params.put("q", "");
        params.put("s", "byIdOrNameOrVillage");
        params.put("startIndex", 0);
        params.put("addressFieldName", "address2");
        params.put("addressFieldValue", "");
        params.put("customAttribute", searchString);
        params.put("patientAttributes", searchArray);
        final JSONArray[] returnValue = new JSONArray[1];

        executeSearch(params, returnValue);
        JSONObject result = returnValue[0].getJSONObject(0);
        assertEquals(searchString, new JSONObject(result.getString("customAttribute")).getString("caste"));

        params.put("customAttribute", true);
        executeSearch(params, returnValue);
        result = returnValue[0].getJSONObject(0);
        assertTrue(new JSONObject(result.getString("customAttribute")).getBoolean("isUrban"));

        searchString = "6th to 9th";
        params.put("customAttribute", searchString);
        executeSearch(params, returnValue);
        result = returnValue[0].getJSONObject(0);
        assertEquals(searchString, new JSONObject(result.getString("customAttribute")).getString("education"));

        params.put("customAttribute", 23);
        executeSearch(params, returnValue);
        result = returnValue[0].getJSONObject(0);
        assertEquals(23, new JSONObject(result.getString("customAttribute")).getInt("landHolding"));
    }

    @Test
    public void testShouldReturnEmptyResultsIfThereAreNoPatientsWithGivenAddress() throws Throwable {

        String searchString = "abc";

        final JSONObject params = new JSONObject();
        params.put("q", "");
        params.put("s", "byIdOrNameOrVillage");
        params.put("startIndex", 0);
        params.put("addressFieldName", "stateProvince");
        params.put("addressFieldValue", searchString);

        final JSONArray[] returnValue = new JSONArray[1];
        JSONObject addressFieldValue = new JSONObject();
        addressFieldValue.put("stateProvince",searchString );

        executeSearch(params, returnValue);

        assertEquals(0, returnValue[0].length());
    }
    @Test
    public void testShouldNotDisplayIfPatientDatecreatedAndEncounterDateIsNotWithinDuration() throws Throwable {


        final JSONObject params = new JSONObject();
        params.put("q", "");
        params.put("s", "byDate");
        params.put("startIndex", 0);
        params.put("addressFieldName", "address2");
        params.put("duration", 14);
        final JSONArray[] returnValue = new JSONArray[1];

        EncounterDbService encounterDbService = new EncounterDbService(mDBHelper);
        String encounterJson = TestUtils.readFileFromAssets("encounter.json", getInstrumentation().getContext());
        JSONObject encounterJsonObj = new JSONObject(encounterJson);

        encounterJsonObj.remove("encounterDateTime");
        encounterJsonObj.put("encounterDateTime", DateTime.now().minusDays(20));
        encounterDbService.insertEncounterData(encounterJsonObj);

        patientData.getJSONObject("patient").getJSONObject("person").getJSONObject("auditInfo").remove("dateCreated");
        patientData.getJSONObject("patient").getJSONObject("person").getJSONObject("auditInfo").put("dateCreated", DateTime.now().minusDays(20));
        patientDbService.insertPatient(patientData);

        executeSearch(params, returnValue);

        assertEquals(returnValue[0].length(), 0);

    }

    @Test
    public void testShouldDisplayIfPatientDatecreatedIsNotWithinDurationAndEncounterDateIsWithinDuration() throws Throwable {

        final JSONObject params = new JSONObject();
        params.put("q", "");
        params.put("s", "byDate");
        params.put("startIndex", 0);
        params.put("addressFieldName", "address2");
        params.put("duration", 14);
        final JSONArray[] returnValue = new JSONArray[1];

        EncounterDbService encounterDbService = new EncounterDbService(mDBHelper);
        String encounterJson = TestUtils.readFileFromAssets("encounter.json", getInstrumentation().getContext());
        JSONObject encounterJsonObj = new JSONObject(encounterJson);

        encounterJsonObj.remove("encounterDateTime");
        encounterJsonObj.put("encounterDateTime", DateTime.now().minusDays(2));
        encounterDbService.insertEncounterData(encounterJsonObj);

        patientData.getJSONObject("patient").getJSONObject("person").getJSONObject("auditInfo").remove("dateCreated");
        patientData.getJSONObject("patient").getJSONObject("person").getJSONObject("auditInfo").put("dateCreated", DateTime.now().minusDays(20));
        patientDbService.insertPatient(patientData);

        executeSearch(params, returnValue);
        JSONObject result = returnValue[0].getJSONObject(0);

        assertEquals(result.getString("givenName"), "test");

    }

    @Test
    public void testShouldDisplayAsRecentPatientIfDateCreatedIsWithinDaterangeAndThereIsNoEncounterForPatient() throws Throwable {

        final JSONObject params = new JSONObject();
        params.put("q", "");
        params.put("s", "byDate");
        params.put("startIndex", 0);
        params.put("addressFieldName", "address2");
        params.put("duration", 14);
        final JSONArray[] returnValue = new JSONArray[1];

        patientData.getJSONObject("patient").getJSONObject("person").getJSONObject("auditInfo").remove("dateCreated");
        patientData.getJSONObject("patient").getJSONObject("person").getJSONObject("auditInfo").put("dateCreated", DateTime.now().minusDays(2));
        patientDbService.insertPatient(patientData);

        executeSearch(params, returnValue);
        JSONObject result = returnValue[0].getJSONObject(0);

        assertEquals(result.getString("givenName"), "test");

    }

    @Test
    public void testShouldDisplayAsRecentPatientIfDatecreatedIsWithinDateRangeAndEncounterDateIsNotInDateRangeForPatient() throws Throwable {

        final JSONObject params = new JSONObject();
        params.put("q", "");
        params.put("s", "byDate");
        params.put("startIndex", 0);
        params.put("addressFieldName", "address2");
        params.put("duration", 14);
        final JSONArray[] returnValue = new JSONArray[1];

        EncounterDbService encounterDbService = new EncounterDbService(mDBHelper);
        String encounterJson = TestUtils.readFileFromAssets("encounter.json", getInstrumentation().getContext());
        JSONObject encounterJsonObj = new JSONObject(encounterJson);

        encounterJsonObj.remove("encounterDateTime");
        encounterJsonObj.put("encounterDateTime", DateTime.now().minusDays(20));
        encounterDbService.insertEncounterData(encounterJsonObj);


        patientData.getJSONObject("patient").getJSONObject("person").getJSONObject("auditInfo").remove("dateCreated");
        patientData.getJSONObject("patient").getJSONObject("person").getJSONObject("auditInfo").put("dateCreated", DateTime.now().minusDays(2));
        patientDbService.insertPatient(patientData);

        executeSearch(params, returnValue);
        JSONObject result = returnValue[0].getJSONObject(0);

        assertEquals(result.getString("givenName"), "test");

    }

    @Test
    public void testShouldDisplayAsRecentPatientIfDatecreatedAndEncounterDateIsWithinDateRange() throws Throwable {

        final JSONObject params = new JSONObject();
        params.put("q", "");
        params.put("s", "byDate");
        params.put("startIndex", 0);
        params.put("addressFieldName", "address2");
        params.put("duration", 14);
        final JSONArray[] returnValue = new JSONArray[1];

        EncounterDbService encounterDbService = new EncounterDbService(mDBHelper);
        String encounterJson = TestUtils.readFileFromAssets("encounter.json", getInstrumentation().getContext());
        JSONObject encounterJsonObj = new JSONObject(encounterJson);

        encounterJsonObj.remove("encounterDateTime");
        encounterJsonObj.put("encounterDateTime", DateTime.now().minusDays(2));
        encounterDbService.insertEncounterData(encounterJsonObj);

        patientData.getJSONObject("patient").getJSONObject("person").getJSONObject("auditInfo").remove("dateCreated");
        patientData.getJSONObject("patient").getJSONObject("person").getJSONObject("auditInfo").put("dateCreated", DateTime.now().minusDays(2));
        patientDbService.insertPatient(patientData);

        executeSearch(params, returnValue);
        JSONObject result = returnValue[0].getJSONObject(0);

        assertEquals(result.getString("givenName"), "test");
    }

}
