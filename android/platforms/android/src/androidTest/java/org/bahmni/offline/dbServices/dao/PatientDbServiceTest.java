package org.bahmni.offline.dbServices.dao;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.bahmni.offline.MainActivity;
import org.bahmni.offline.Utils.TestUtils;
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

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_PATIENT_TABLE);

        PatientDbService patientDbService = new PatientDbService(mDBHelper);
        String patientJson = TestUtils.readFileFromAssets("patient.json", getInstrumentation().getContext());

        patientDbService.insertPatient(new JSONObject(patientJson));

        String uuid = "e34992ca-894f-4344-b4b3-54a4aa1e5558";
        JSONObject result = patientDbService.getPatientByUuid(uuid);
        assertEquals(uuid, result.getJSONObject("patient").getString("uuid"));
    }

}
