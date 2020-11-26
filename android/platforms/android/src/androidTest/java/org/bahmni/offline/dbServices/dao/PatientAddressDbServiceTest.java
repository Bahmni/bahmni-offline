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


public class PatientAddressDbServiceTest extends ActivityInstrumentationTestCase2<MainActivity> {


    public PatientAddressDbServiceTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        super(MainActivity.class);
    }

    @Test
    public void testShouldCreatePatientAddress() throws Exception {

        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db", 5);
        mDBHelper.createTable(Constants.CREATE_PATIENT_TABLE);
        mDBHelper.createTable(Constants.CREATE_PATIENT_ADDRESS_TABLE);

        PatientDbService patientDbService = new PatientDbService();
        PatientAddressDbService patientAddressDbService = new PatientAddressDbService(mDBHelper);
        JSONObject patientData = new JSONObject(TestUtils.readFileFromAssets("patient.json", getInstrumentation().getContext()));
        JSONObject address = patientData.getJSONObject("patient").getJSONObject("person").getJSONObject("preferredAddress");

        String uuid = "e34992ca-894f-4344-b4b3-54a4aa1e5558";
        patientDbService.insertPatient(patientData, mDBHelper);
        patientAddressDbService.insertAddress(address, uuid);

        JSONObject result = patientDbService.getPatientByUuid(uuid, mDBHelper);
        JSONObject returnedAddress = result.getJSONObject("patient").getJSONObject("person").getJSONObject("preferredAddress");

        assertEquals("PACHARI", returnedAddress.getString("cityVillage"));
        assertEquals("Chattisgarh", returnedAddress.getString("stateProvince"));
        assertEquals("Raipur", returnedAddress.getString("countyDistrict"));
        assertEquals("BILAIGARH", returnedAddress.getString("address3"));
        assertEquals("BILAIGARH", returnedAddress.getString("address3"));
    }

}
