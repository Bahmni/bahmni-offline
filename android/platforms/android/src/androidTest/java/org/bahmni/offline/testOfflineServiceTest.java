package org.bahmni.offline;

import android.content.Context;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import org.bahmni.offline.db.AttributeService;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

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

public class testOfflineServiceTest extends ActivityInstrumentationTestCase2<MainActivity>{


    String host;

    public testOfflineServiceTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        super(MainActivity.class);
        ignoreCertificates();
        Bundle arguments = InstrumentationRegistry.getArguments();
        host = "https://10.4.23.44:8082";
        if(arguments.containsKey("host"))
            host =  arguments.getString("host");
    }

    private void ignoreCertificates() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[] {
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[]{}; }
                }
        }, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
    }

    @Test
    public void testShouldPrint() throws Exception {

        Util util = Mockito.mock(Util.class);
        when(util.getData(any(URL.class))).thenReturn(readFileFromAssets("patientAttributes.json", InstrumentationRegistry.getContext()));

        Context context = InstrumentationRegistry.getTargetContext();
        OfflineService offlineService = new OfflineService(context, new AttributeService(util));
        offlineService.populateData(host);
        String patientJson = readFileFromAssets("patient.json", InstrumentationRegistry.getContext());
        offlineService.createPatient(patientJson, "GET");
        String uuid = "e34992ca-894f-4344-b4b3-54a4aa1e5558";
        JSONObject result = new JSONObject(offlineService.getPatientByUuid(uuid));
        assertEquals(uuid, result.getJSONObject("patient").getString("uuid"));
    }


    public static String readFileFromAssets(String fileName, Context c) {
        try {
            InputStream is = c.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String text = new String(buffer);

            return text;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
