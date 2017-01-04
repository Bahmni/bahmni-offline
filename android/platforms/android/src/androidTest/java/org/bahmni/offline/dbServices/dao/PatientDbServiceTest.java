package org.bahmni.offline.dbServices.dao;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.bahmni.offline.MainActivity;
import org.bahmni.offline.Utils.TestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class PatientDbServiceTest extends ActivityInstrumentationTestCase2<MainActivity>{


    public PatientDbServiceTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        super(MainActivity.class);
    }

    @Test
    public void testShouldCreatePatient() throws Exception {

        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db", 5);
        mDBHelper.createTable(Constants.CREATE_PATIENT_TABLE);

        PatientDbService patientDbService = new PatientDbService(mDBHelper);
        String patientJson = TestUtils.readFileFromAssets("patient.json", getInstrumentation().getContext());

        patientDbService.insertPatient(new JSONObject(patientJson));

        String uuid = "e34992ca-894f-4344-b4b3-54a4aa1e5558";
        JSONObject result = patientDbService.getPatientByUuid(uuid);
        assertEquals(uuid, result.getJSONObject("patient").getString("uuid"));
    }

    @Test
    public void testShouldCreatePatientWithPreferredNameIfNamesIsNull() throws Exception {

        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db", 5);
        mDBHelper.createTable(Constants.CREATE_PATIENT_TABLE);

        PatientDbService patientDbService = new PatientDbService(mDBHelper);
        String patientJson = TestUtils.readFileFromAssets("patient.json", getInstrumentation().getContext());

        JSONObject patientData = new JSONObject(patientJson);
        JSONObject patient = patientData.getJSONObject("patient");
        JSONObject person = patient.getJSONObject("person");
        person.put("names", new JSONArray());
        JSONObject preferredNameJson = person.getJSONObject("preferredName");
        preferredNameJson.put("givenName","preferredGivenName");
        preferredNameJson.put("middleName","preferredMiddleName");
        preferredNameJson.put("familyName","preferredFamilyName");
        person.put("preferredName", preferredNameJson);

        patientDbService.insertPatient(patientData);

        String uuid = "e34992ca-894f-4344-b4b3-54a4aa1e5558";
        JSONObject result = patientDbService.getPatientByUuid(uuid);

        assertNotNull(result);
        JSONObject actualPreferredNames = result.getJSONObject("patient").getJSONObject("person").getJSONObject("preferredName");
        assertEquals("preferredGivenName", actualPreferredNames.getString("givenName"));
        assertEquals("preferredMiddleName", actualPreferredNames.getString("middleName"));
        assertEquals("preferredFamilyName", actualPreferredNames.getString("familyName"));
    }

    @Test
    public void testShouldNotFetchVoidedPatients() throws Exception {

        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db", 5);
        mDBHelper.createTable(Constants.CREATE_PATIENT_TABLE);

        PatientDbService patientDbService = new PatientDbService(mDBHelper);
        String patientJson = TestUtils.readFileFromAssets("patient.json", getInstrumentation().getContext());

        JSONObject patientData = new JSONObject(patientJson);
        JSONObject patient = patientData.getJSONObject("patient");
        patient.put("voided", true);
        patientDbService.insertPatient(patientData);

        String uuid = "e34992ca-894f-4344-b4b3-54a4aa1e5558";
        JSONObject result = patientDbService.getPatientByUuid(uuid);
        assertNull(result);
    }


}
