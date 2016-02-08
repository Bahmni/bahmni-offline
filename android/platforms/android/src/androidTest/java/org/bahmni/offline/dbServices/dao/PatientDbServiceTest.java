package org.bahmni.offline.dbServices.dao;

import android.content.Context;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.bahmni.offline.MainActivity;
import org.bahmni.offline.Util;
import org.bahmni.offline.Utils.TestUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)

public class PatientDbServiceTest extends ActivityInstrumentationTestCase2<MainActivity>{


    public PatientDbServiceTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        super(MainActivity.class);
    }

    @Test
    public void ShouldCreatePatient() throws Exception {

        Context context = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase.loadLibs(context);

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_PATIENT_TABLE);

        PatientDbService patientDbService = new PatientDbService(mDBHelper);
        String patientJson = TestUtils.readFileFromAssets("patient.json", InstrumentationRegistry.getContext());

        patientDbService.insertPatient(new JSONObject(patientJson));

        String uuid = "e34992ca-894f-4344-b4b3-54a4aa1e5558";
        JSONObject result = patientDbService.getPatientByUuid(uuid);
        assertEquals(uuid, result.getJSONObject("patient").getString("uuid"));
    }

}
