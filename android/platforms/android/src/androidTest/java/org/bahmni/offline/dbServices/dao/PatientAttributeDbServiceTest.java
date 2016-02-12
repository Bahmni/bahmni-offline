package org.bahmni.offline.dbServices.dao;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.bahmni.offline.MainActivity;
import org.bahmni.offline.Util;
import org.bahmni.offline.Utils.TestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


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

        Util util = Mockito.mock(Util.class);
        when(util.getData(any(URL.class))).thenReturn(TestUtils.readFileFromAssets("patientAttributeTypes.json", getInstrumentation().getContext()));

        SQLiteDatabase db =  mDBHelper.getWritableDatabase(Constants.KEY);

        ArrayList<JSONObject> attributeTypeMap = TestUtils.getAttributeTypeMap(db);

        JSONObject person = patientData.getJSONObject("patient").getJSONObject("person");
        JSONArray attributes = person.getJSONArray("attributes");
        String uuid = "e34992ca-894f-4344-b4b3-54a4aa1e5558";
        PatientAttributeDbService patientAttributeDbService = new PatientAttributeDbService(mDBHelper, util);
        patientAttributeDbService.insertAttributes(uuid, attributes, attributeTypeMap);

        JSONObject result = patientDbService.getPatientByUuid(uuid);

        JSONObject returnedPatient = result.getJSONObject("patient");
        JSONArray returnedAttributes = returnedPatient.getJSONObject("person").getJSONArray("attributes");

        for (int i = 0; i < returnedAttributes.length(); i++) {
            JSONObject returnedAttribute = returnedAttributes.getJSONObject(i);
            if(returnedAttribute.getJSONObject("attributeType").getString("display").equals("caste")) {
                assertEquals("hindu", returnedAttribute.getString("value"));
            }
            if(returnedAttribute.getJSONObject("attributeType").getString("display").equals("class")) {
                assertEquals("General", returnedAttribute.getString("display"));
            }
            if(returnedAttribute.getJSONObject("attributeType").getString("display").equals("education")) {
                assertEquals("6th to 9th", returnedAttribute.getString("display"));
            }
            if(returnedAttribute.getJSONObject("attributeType").getString("display").equals("occupation")) {
                assertEquals("Government", returnedAttribute.getString("display"));
            }
            if(returnedAttribute.getJSONObject("attributeType").getString("display").equals("landHolding")) {
                assertEquals(23, returnedAttribute.getInt("value"));
            }
            if(returnedAttribute.getJSONObject("attributeType").getString("display").equals("debt")) {
                assertEquals("21", returnedAttribute.getString("value"));
            }
            if(returnedAttribute.getJSONObject("attributeType").getString("display").equals("isUrban")) {
                assertEquals(true, returnedAttribute.getBoolean("value"));
            }
            if(returnedAttribute.getJSONObject("attributeType").getString("display").equals("cluster")) {
                assertEquals("Shivtarai2", returnedAttribute.getString("display"));
            }
        }
    }


}
