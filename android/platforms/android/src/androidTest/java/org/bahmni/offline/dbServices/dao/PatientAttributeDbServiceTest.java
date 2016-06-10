package org.bahmni.offline.dbServices.dao;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.bahmni.offline.MainActivity;
import org.bahmni.offline.Utils.TestUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;




public class PatientAttributeDbServiceTest extends ActivityInstrumentationTestCase2<MainActivity>{
            public PatientAttributeDbServiceTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        super(MainActivity.class);
    }

    public void testShouldCreatePatientAttributes() throws Exception {

        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);
        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_PATIENT_TABLE);
        mDBHelper.createTable(Constants.CREATE_PATIENT_ATTRIBUTE_TYPE_TABLE);
        mDBHelper.createTable(Constants.CREATE_PATIENT_ATTRIBUTE_TABLE);

        PatientDbService patientDbService = new PatientDbService(mDBHelper);
        String patientJson = TestUtils.readFileFromAssets("patient.json", getInstrumentation().getContext());
        JSONObject patientData = new JSONObject(patientJson);
        patientDbService.insertPatient(patientData);

        SQLiteDatabase db =  mDBHelper.getWritableDatabase();

        ArrayList<JSONObject> attributeTypeMap = TestUtils.getAttributeTypeMap(db);

        JSONObject person = patientData.getJSONObject("patient").getJSONObject("person");
        JSONArray attributes = person.getJSONArray("attributes");
        String uuid = "e34992ca-894f-4344-b4b3-54a4aa1e5558";
        PatientAttributeDbService patientAttributeDbService = new PatientAttributeDbService(mDBHelper);
        patientAttributeDbService.insertAttributes(uuid, attributes);

        JSONObject result = patientDbService.getPatientByUuid(uuid);

        JSONObject returnedPatient = result.getJSONObject("patient");
        JSONArray returnedAttributes = returnedPatient.getJSONObject("person").getJSONArray("attributes");

        for (int i = 0; i < returnedAttributes.length(); i++) {
            JSONObject returnedAttribute = returnedAttributes.getJSONObject(i);
            if(returnedAttribute.getJSONObject("attributeType").getString("uuid").equals("c1f4239f-3f10-11e4-adec-0800271c1b75")) {
                assertEquals("hindu", returnedAttribute.getString("value"));
            }
            if(returnedAttribute.getJSONObject("attributeType").getString("uuid").equals("c1f455e7-3f10-11e4-adec-0800271c1b75")) {
                assertEquals("General", returnedAttribute.getString("value"));
            }
            if(returnedAttribute.getJSONObject("attributeType").getString("uuid").equals("c1f4a004-3f10-11e4-adec-0800271c1b75")) {
                assertEquals("6th to 9th", returnedAttribute.getString("value"));
            }
            if(returnedAttribute.getJSONObject("attributeType").getString("uuid").equals("3dfdc176-17fd-42b1-b5be-c7e25b78b602")) {
                assertEquals(23, returnedAttribute.getInt("value"));
            }
            if(returnedAttribute.getJSONObject("attributeType").getString("uuid").equals("fb3c00b1-81c8-40fe-89e8-6b3344688a13")) {
                assertEquals("21", returnedAttribute.getString("value"));
            }
            if(returnedAttribute.getJSONObject("attributeType").getString("uuid").equals("9234695b-0f68-4970-aeb7-3b32d4a2b346")) {
                assertEquals(true, returnedAttribute.getBoolean("value"));
            }
            if(returnedAttribute.getJSONObject("attributeType").getString("uuid").equals("35e98d04-3981-4257-a593-fadd81bfc109")) {
                assertEquals("Shivtarai", returnedAttribute.getString("value"));
            }
        }
    }


}
