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
import java.util.Date;

public class ObservationDbServiceTest extends ActivityInstrumentationTestCase2<MainActivity>{

    public ObservationDbServiceTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        super(MainActivity.class);
    }

    @Test
    public void testShouldInsertObservations() throws Exception {

        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        String patientUuid = "fc6ede09-f16f-4877-d2f5-ed8b2182ec11";
        String visitUuid = "47a706a2-c0e6-4e40-ae31-4a3535be2ace";
        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_OBSERVATION_TABLE);

        String encounterJson = TestUtils.readFileFromAssets("encounter.json", getInstrumentation().getContext());
        JSONObject encounter = new JSONObject(encounterJson);
        JSONArray observationJson = encounter.getJSONArray("observations");

        ObservationDbService observationDbService = new ObservationDbService(mDBHelper);

       observationDbService.insertObservationData(patientUuid,visitUuid, observationJson);

        JSONObject params = new JSONObject();
        params.put("patientUuid", patientUuid);
        params.put("visitUuids", new JSONArray().put(0, visitUuid));
        params.put("conceptNames", new JSONArray().put(0, "Child Health"));
        JSONArray observations = observationDbService.getObservationsFor(params);

        JSONObject observation = observations.getJSONObject(0);

        assertEquals(observation.getJSONObject("observationJson").getJSONArray("groupMembers").length(), 5);

    }

    @Test
    public void testShouldRemoveObservationsIfTheObservationDataIsRemovedFromEncounter() throws Exception {

        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        String patientUuid = "fc6ede09-f16f-4877-d2f5-ed8b2182ec11";
        String visitUuid = "47a706a2-c0e6-4e40-ae31-4a3535be2ace";
        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_OBSERVATION_TABLE);

        String encounterJson = TestUtils.readFileFromAssets("encounter.json", getInstrumentation().getContext());
        JSONObject encounter = new JSONObject(encounterJson);
        JSONArray observationJson = encounter.getJSONArray("observations");

        ObservationDbService observationDbService = new ObservationDbService(mDBHelper);
        observationDbService.insertObservationData(patientUuid,visitUuid, observationJson);

        observationJson.getJSONObject(0).put("groupMembers", new JSONArray());
        encounter.put("observations", observationJson);

        observationDbService.insertObservationData(patientUuid,visitUuid, observationJson);

        JSONObject params = new JSONObject();
        params.put("patientUuid", patientUuid);
        params.put("visitUuids", new JSONArray().put(0, visitUuid));
        params.put("conceptNames", new JSONArray().put(0, "Child Health"));
        JSONArray observations = observationDbService.getObservationsFor(params);

        assertEquals(observations, null);

    }

    @Test
    public void testShouldInsertObservationsWhenVisitUuidNull() throws Exception {

        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        String patientUuid = "fc6ede09-f16f-4877-d2f5-ed8b2182ec11";
        String visitUuid = null;
        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_OBSERVATION_TABLE);

        String encounterJson = TestUtils.readFileFromAssets("encounter.json", getInstrumentation().getContext());
        JSONObject encounter = new JSONObject(encounterJson);
        JSONArray observationJson = encounter.getJSONArray("observations");

        ObservationDbService observationDbService = new ObservationDbService(mDBHelper);

        observationDbService.insertObservationData(patientUuid,visitUuid, observationJson);

        JSONObject params = new JSONObject();
        params.put("patientUuid", patientUuid);
        params.put("visitUuids", new JSONArray());
        params.put("conceptNames", new JSONArray().put(0, "Child Health"));
        JSONArray observations = observationDbService.getObservationsFor(params);

        JSONObject observation = observations.getJSONObject(0);

        assertEquals(observation.getJSONObject("observationJson").getJSONArray("groupMembers").length(), 5);

    }

    @Test
    public void testShouldInsertObsAndReturnObsGroupByEncounter() throws Exception {

        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        String patientUuid = "fc6ede09-f16f-4877-d2f5-ed8b2182ec11";
        String visitUuid = null;
        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_OBSERVATION_TABLE);

        String encounterJson = TestUtils.readFileFromAssets("encounter.json", getInstrumentation().getContext());
        JSONObject encounter = new JSONObject(encounterJson);
        JSONArray observationJson = encounter.getJSONArray("observations");

        ObservationDbService observationDbService = new ObservationDbService(mDBHelper);

        observationDbService.insertObservationData(patientUuid,visitUuid, observationJson);

        observationJson.getJSONObject(0).put("encounterDateTime", new Date().getTime());
        observationJson.getJSONObject(0).put("encounterUuid", "1c5c237a-dc6e-4f4f-bcff-c761c1ae5975");
        observationJson.getJSONObject(0).put( "uuid", "b5c88093-769d-4c21-9249-d8598e30627");
        observationDbService.insertObservationData(patientUuid,visitUuid, observationJson);

        JSONObject params = new JSONObject();
        params.put("patientUuid", patientUuid);
        params.put("visitUuids", new JSONArray());
        params.put("conceptNames", new JSONArray().put(0, "Child Health"));
        JSONArray observations = observationDbService.getObservationsFor(params);

        assertEquals(observations.length(), 2);

    }
}
