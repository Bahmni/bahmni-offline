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

public class VisitDbServiceTest extends ActivityInstrumentationTestCase2<MainActivity>{

    public VisitDbServiceTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        super(MainActivity.class);
    }

    @Test
    public void testShouldInsertVisitAndGetVisitByUuid() throws Exception {

        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        String uuid = "de5d8f4b-cb75-4eff-8637-fd0efc0fb9ad";
        String patientUuid = "d07ddb7e-fd8d-4e06-bc44-3bb17507d955";
        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db", 5);
        mDBHelper.createTable(Constants.CREATE_VISIT_TABLE);

        VisitDbService visitDbService = new VisitDbService(mDBHelper);
        String visitJson = TestUtils.readFileFromAssets("visit.json", getInstrumentation().getContext());

        visitDbService.insertVisitData(new JSONObject(visitJson), null);

        JSONObject visit = visitDbService.getVisitByUuid(uuid);

        assertEquals(uuid, visit.getString("uuid"));
        assertEquals(patientUuid, visit.getJSONObject("visitJson").getJSONObject("patient").getString("uuid"));
        assertEquals(2, visit.getJSONObject("visitJson").getJSONArray("encounters").length());

    }

    @Test
    public void testShouldGetVisitsByPatientByUuid() throws  Exception {

        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        int numberOfVisits = 2;
        String patientUuid = "d07ddb7e-fd8d-4e06-bc44-3bb17507d955";
        DateTime startDateTime = new DateTime("2016-04-26T17:36:18.000+0530");

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db", 5);
        mDBHelper.createTable(Constants.CREATE_VISIT_TABLE);

        VisitDbService visitDbService = new VisitDbService(mDBHelper);
        String visitJson = TestUtils.readFileFromAssets("visit.json", getInstrumentation().getContext());

        JSONObject visit = new JSONObject(visitJson);
        visitDbService.insertVisitData(visit, null);

        visit.put("uuid", "fe5d8f4b-cb75-4eff-8637-fd0efc0fb9ad");
        visitDbService.insertVisitData(visit, null);

        JSONArray visits = visitDbService.getVisitsByPatientUuid(patientUuid ,numberOfVisits);

        assertEquals(visits.length(), 2);
        assertEquals("de5d8f4b-cb75-4eff-8637-fd0efc0fb9ad", visits.getJSONObject(0).getString("uuid"));
        assertEquals(startDateTime, new DateTime(visits.getJSONObject(1).getString("startDatetime")));
    }

    @Test
    public void testShouldGetVisitDetailsByPatientByUuid() throws  Exception {

        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        String patientUuid = "d07ddb7e-fd8d-4e06-bc44-3bb17507d955";
        DateTime startDateTime = new DateTime("2016-04-26T17:36:18.000+0530");

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db", 5);
        mDBHelper.createTable(Constants.CREATE_VISIT_TABLE);

        VisitDbService visitDbService = new VisitDbService(mDBHelper);
        String visitJson = TestUtils.readFileFromAssets("visit.json", getInstrumentation().getContext());

        JSONObject visit = new JSONObject(visitJson);
        visitDbService.insertVisitData(visit, null);

        visit.put("uuid", "fe5d8f4b-cb75-4eff-8637-fd0efc0fb9ad");
        visit.getJSONObject("patient").put("uuid", "another uuid");
        visitDbService.insertVisitData(visit, null);

        JSONArray visits = visitDbService.getVisitDetailsByPatientUuid(patientUuid);

        assertEquals(1, visits.length());
        assertEquals("de5d8f4b-cb75-4eff-8637-fd0efc0fb9ad", new JSONObject(visits.getString(0)).getString("uuid"));
    }
}
