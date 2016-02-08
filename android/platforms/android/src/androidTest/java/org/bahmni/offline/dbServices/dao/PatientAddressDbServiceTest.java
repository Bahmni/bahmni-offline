//package org.bahmni.offline.dbServices.dao;
//
//import android.content.Context;
//import android.support.test.InstrumentationRegistry;
//import android.support.test.runner.AndroidJUnit4;
//import android.test.ActivityInstrumentationTestCase2;
//import net.sqlcipher.database.SQLiteDatabase;
//import org.bahmni.offline.Constants;
//import org.bahmni.offline.MainActivity;
//import org.bahmni.offline.Utils.TestUtils;
//import org.json.JSONObject;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.io.IOException;
//import java.security.KeyManagementException;
//import java.security.NoSuchAlgorithmException;
//
//@RunWith(AndroidJUnit4.class)
//
//public class PatientAddressDbServiceTest extends ActivityInstrumentationTestCase2<MainActivity>{
//
//
//    public PatientAddressDbServiceTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
//        super(MainActivity.class);
//    }
//
//    @Test
//    public void ShouldCreatePatientAddress() throws Exception {
//
//        Context context = InstrumentationRegistry.getTargetContext();
//        SQLiteDatabase.loadLibs(context);
//
//        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
//        mDBHelper.createTable(Constants.CREATE_PATIENT_TABLE);
//        mDBHelper.createTable(Constants.CREATE_PATIENT_ADDRESS_TABLE);
//
//        PatientDbService patientDbService = new PatientDbService(mDBHelper);
//        PatientAddressDbService patientAddressDbService = new PatientAddressDbService(mDBHelper);
//        JSONObject patientData = new JSONObject(TestUtils.readFileFromAssets("patient.json", InstrumentationRegistry.getContext()));
//        JSONObject address = patientData.getJSONObject("patient").getJSONObject("person").getJSONObject("preferredAddress");
//
//        String uuid = "e34992ca-894f-4344-b4b3-54a4aa1e5558";
//        patientDbService.insertPatient(patientData);
//        patientAddressDbService.insertAddress(address, uuid);
//
//        JSONObject result = patientDbService.getPatientByUuid(uuid);
//        assertEquals(uuid, result.getJSONObject("patient").getString("uuid"));
//    }
//
//}
