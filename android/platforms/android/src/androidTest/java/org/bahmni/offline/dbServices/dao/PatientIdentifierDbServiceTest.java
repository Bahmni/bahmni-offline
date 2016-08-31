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
import java.util.List;
import java.util.StringTokenizer;

public class PatientIdentifierDbServiceTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public PatientIdentifierDbServiceTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        super(MainActivity.class);
    }

    @Test
    public void shouldCreatePatientIdentifiers() throws Exception {
        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_PATIENT_IDENTIFIER_TABLE);
        PatientIdentifierDbService patientIdentifierDbService = new PatientIdentifierDbService(mDBHelper);

        JSONObject patientData = new JSONObject(TestUtils.readFileFromAssets("patient.json", getInstrumentation().getContext()));
        String patientUuid = "e34992ca-894f-4344-b4b3-54a4aa1e5558";
        JSONArray identifiers = patientData.getJSONObject("patient").getJSONArray("identifiers");

        identifiers.getJSONObject(0).put("primaryIdentifier", "GAN200076");
        JSONObject extraIdentifiers = new JSONObject();
        extraIdentifiers.put("Secondary", "SEC202020");
        identifiers.getJSONObject(0).put("extraIdentifiers", extraIdentifiers);
        patientIdentifierDbService.insertPatientIdentifiers(patientUuid, identifiers);

        String patientIdentifiersByPatientUuidFromDb = patientIdentifierDbService.getPatientIdentifiersByPatientUuid(patientUuid);
        assertEquals(new JSONArray(patientIdentifiersByPatientUuidFromDb).getJSONObject(0).getString("identifier"), "GAN200076");
        assertEquals(new JSONArray(patientIdentifiersByPatientUuidFromDb).getJSONObject(0).getString("extraIdentifiers"), extraIdentifiers.toString());
    }
}
