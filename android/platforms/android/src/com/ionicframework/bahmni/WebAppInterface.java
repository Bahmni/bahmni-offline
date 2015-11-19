package com.ionicframework.bahmni;

/**
 * Created by TWI on 16/11/15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.JavascriptInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;


/**
 * Created by TWI on 13/11/15.
 */
public class WebAppInterface {
    Context mContext;
    private DbHelper mDbHelper;
    int id=0;


    /** Instantiate the interface and set the context */
    WebAppInterface(Context c) {
        mContext = c;
        mDbHelper = new DbHelper(c);
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }


    @JavascriptInterface
    public void populateData() throws IOException, JSONException, CertificateException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("admin", "test".toCharArray());
            }
        });

        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        URL obj = new URL("https://10.4.22.129:8082/openmrs/ws/rest/v1/patientprofile/all");

        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setHostnameVerifier(hostnameVerifier);              //TODO : Have to fix this
        int responseCode = con.getResponseCode();
        InputStream inputStream = con.getInputStream();
        JSONArray patients = new JSONArray(convertStreamToString(inputStream));
        for (int i = 0; i < patients.length(); i++) {

            ContentValues values = new ContentValues();
            JSONObject patient = new JSONObject(patients.getJSONObject(i).getString("patient"));
            JSONObject patientName = new JSONObject(new JSONObject(patient.getString("person")).getString("preferredName"));

            values.put(DBContract.Entry.COLUMN_PATIENT_ID, new JSONArray(patient.getString("identifiers")).getJSONObject(0).getString("identifier"));
            values.put(DBContract.Entry.COLUMN_PATIENT_UUID, patient.getString("uuid"));
            values.put(DBContract.Entry.COLUMN_PATIENT_FIRST_NAME, patientName.getString("givenName"));
            values.put(DBContract.Entry.COLUMN_PATIENT_MIDDLE_NAME, patientName.getString("middleName"));
            values.put(DBContract.Entry.COLUMN_PATIENT_LAST_NAME, patientName.getString("familyName"));
            values.put(DBContract.Entry.COLUMN_PATIENT_JSON, String.valueOf(patients.getJSONObject(i)));
            db.insert(DBContract.Entry.TABLE_NAME, null, values);
        }
    }

    @JavascriptInterface
    public void insertData()  {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        for(int i = 0 ; i < 100; i++){
            ContentValues values = new ContentValues();
            values.put(DBContract.Entry.COLUMN_PATIENT_JSON, "yayvdkxehwvkhjvekwfvwqejhfkwehdvfjwhvljhfvljhfvlwerhvfkwjehvfkjwhevfljhwevlfjhvwelfjhvwelfjhvwehfvwehflewfvhev" + i);
            db.insert( DBContract.Entry.TABLE_NAME, null, values);
        }
    }

    @JavascriptInterface
    public String getPatient(String uuid){
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * from " + DBContract.Entry.TABLE_NAME +
                               " WHERE " + DBContract.Entry.COLUMN_PATIENT_UUID + " = '" + uuid + "' limit 1 ", new String[]{});
        c.moveToFirst();
        return c.getString(6);
    }

    @JavascriptInterface
    public String search(String searchTerm, int offset) throws JSONException {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * from " + DBContract.Entry.TABLE_NAME +
                               " WHERE " + DBContract.Entry.COLUMN_PATIENT_FIRST_NAME + " LIKE '%" + searchTerm +
                               "%' OR " + DBContract.Entry.COLUMN_PATIENT_MIDDLE_NAME + " LIKE '%" + searchTerm +
                               "%' OR " + DBContract.Entry.COLUMN_PATIENT_FIRST_NAME + " LIKE '%" + searchTerm +
                               "%' OR " + DBContract.Entry.COLUMN_PATIENT_LAST_NAME + " LIKE '%" + searchTerm +
                               "%' OR " + DBContract.Entry.COLUMN_PATIENT_ID + " LIKE '%" + searchTerm +
                               "%' LIMIT 50 OFFSET " + offset, new String[]{});
        c.moveToFirst();
        JSONArray json = new JSONArray();
        do {
            JSONObject obj = new JSONObject();
            obj.put("identifier", c.getString(1));
            obj.put("givenName", c.getString(3));
            obj.put("middleName", c.getString(4));
            obj.put("familyName", c.getString(5));
            obj.put("uuid", c.getString(2));
            json.put(obj);
        }while(c.moveToNext());

        return String.valueOf(new JSONObject().put("pageOfResults", json));

    }

    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


}
