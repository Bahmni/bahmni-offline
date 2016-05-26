package org.bahmni.offline.dbServices.dao;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.bahmni.offline.MainActivity;
import org.bahmni.offline.Util;
import org.bahmni.offline.Utils.TestUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class EncounterDbServiceTest extends ActivityInstrumentationTestCase2<MainActivity>{

    public EncounterDbServiceTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        super(MainActivity.class);
    }

    @Test
    public void testShouldInsertEncountersAndGetEncounterByPatientUuid() throws Exception {

        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        String uuid = "1c5c237a-dc6e-4f4f-bcff-c761c1ae5972";
        String patientUuid = "fc6ede09-f16f-4877-d2f5-ed8b2182ec11";
        DateTime encounterDateTime = new DateTime("2016-04-22T11:06:20.000+0530");
        String visitUuid = "47a706a2-c0e6-4e40-ae31-4a3535be2ace";
        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_ENCOUNTER_TABLE);

        EncounterDbService encounterDbService = new EncounterDbService(mDBHelper);
        String encounterJson = TestUtils.readFileFromAssets("encounter.json", getInstrumentation().getContext());

        encounterDbService.insertEncounterData(new JSONObject(encounterJson));

        JSONArray encounters = encounterDbService.getEncountersByPatientUuid(patientUuid);

        assertEquals(uuid, encounters.getJSONObject(0).getJSONObject("encounter").getString("encounterUuid"));
        assertEquals(patientUuid, encounters.getJSONObject(0).getJSONObject("encounter").getString("patientUuid"));
        assertEquals(visitUuid, encounters.getJSONObject(0).getJSONObject("encounter").getString("visitUuid"));
        assertEquals(encounterDateTime, new DateTime(encounters.getJSONObject(0).getJSONObject("encounter").getString("encounterDateTime")));
    }

    @Test
    public void  testShouldGetActiveEncounterForThePatient() throws Exception {
        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        String patientUuid = "fc6ede09-f16f-4877-d2f5-ed8b2182ec11";
        String providerUuid = "6a5d9c71-bb71-47ad-abed-bda86637f1b7";

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_ENCOUNTER_TABLE);

        EncounterDbService encounterDbService = new EncounterDbService(mDBHelper);
        String encounterJson = TestUtils.readFileFromAssets("encounter.json", getInstrumentation().getContext());

        JSONObject encounter = new JSONObject(encounterJson);
        encounter.put("encounterDateTime", Util.addMinutesToDate(-10, new Date()).toString());

        encounterDbService.insertEncounterData(encounter);

        JSONObject params = new JSONObject();
        params.put("patientUuid", patientUuid);
        params.put("providerUuid", providerUuid);
        params.put("encounterType", "FIELD");

        JSONObject activeEncounter = encounterDbService.findActiveEncounter(params, 60);

        assertEquals("1c5c237a-dc6e-4f4f-bcff-c761c1ae5972", activeEncounter.getJSONObject("encounter").getString("encounterUuid"));
        assertEquals(patientUuid, activeEncounter.getJSONObject("encounter").getString("patientUuid"));

    }

    @Test
    public void  testShouldGetEncounterByEncounterUuid() throws Exception {
        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_ENCOUNTER_TABLE);

        EncounterDbService encounterDbService = new EncounterDbService(mDBHelper);
        String encounterJson = TestUtils.readFileFromAssets("encounter.json", getInstrumentation().getContext());

        JSONObject encounter = new JSONObject(encounterJson);
        encounter.put("encounterDateTime", Util.addMinutesToDate(-10, new Date()).toString());

        encounterDbService.insertEncounterData(encounter);

        String encounterUuid = "1c5c237a-dc6e-4f4f-bcff-c761c1ae5972";
        JSONObject encounterObject = encounterDbService.findEncounterByEncounterUuid(encounterUuid);

        assertNotNull(encounterObject);
        assertEquals("1c5c237a-dc6e-4f4f-bcff-c761c1ae5972", encounterObject.getJSONObject("encounter").getString("encounterUuid"));
    }

    @Test
    public void  testShouldGetEncounterByVisit() throws Exception {
        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_ENCOUNTER_TABLE);

        EncounterDbService encounterDbService = new EncounterDbService(mDBHelper);
        String encounterJson = TestUtils.readFileFromAssets("encounter.json", getInstrumentation().getContext());

        JSONObject encounter = new JSONObject(encounterJson);
        encounter.put("encounterDateTime", Util.addMinutesToDate(-10, new Date()).toString());

        encounterDbService.insertEncounterData(encounter);

        String patientUuid = "fc6ede09-f16f-4877-d2f5-ed8b2182ec11";
        JSONArray visitUuids = new JSONArray();
        visitUuids.put(0, "47a706a2-c0e6-4e40-ae31-4a3535be2ace");
        JSONObject params = new JSONObject();
        params.put("patientUuid", patientUuid);
        params.put("visitUuids", visitUuids);

        JSONArray encounterList = encounterDbService.getEncountersByVisits(params);

        assertNotNull(encounterList);
        assertEquals(1, encounterList.length());
        assertEquals("1c5c237a-dc6e-4f4f-bcff-c761c1ae5972", encounterList.getJSONObject(0).getJSONObject("encounter").getString("encounterUuid"));
    }
}
