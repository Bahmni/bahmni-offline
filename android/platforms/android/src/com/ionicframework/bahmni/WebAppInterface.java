package com.ionicframework.bahmni;

/**
 * Created by TWI on 16/11/15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.JavascriptInterface;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;


/**
 * Created by TWI on 13/11/15.
 */
public class WebAppInterface {
    Context mContext;
    private DbHelper mDbHelper;
    String[] patientColumnNames = {DBContract.PATIENT.COLUMN_PATIENT_ID, DBContract.PATIENT.COLUMN_PATIENT_UUID, DBContract.PATIENT.COLUMN_PATIENT_FIRST_NAME, DBContract.PATIENT.COLUMN_PATIENT_MIDDLE_NAME,
            DBContract.PATIENT.COLUMN_PATIENT_LAST_NAME, DBContract.PATIENT.COLUMN_PATIENT_GENDER, DBContract.PATIENT.COLUMN_PATIENT_AGE, DBContract.PATIENT.COLUMN_DATE_CREATED,
            DBContract.PATIENT.COLUMN_RURAL_WARD, DBContract.PATIENT.COLUMN_PATIENT_JSON};
    String[] attributeColumnNames;
    SharedPreferences sharedPref;




    /** Instantiate the interface and set the context */
    WebAppInterface(Context c) {
        mContext = c;
        mDbHelper = new DbHelper(c);
        sharedPref =  mContext.getSharedPreferences("SQL Select Statement", Context.MODE_PRIVATE);
    }

    HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

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

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        mDbHelper.createTable(db, DBContract.PATIENT.TABLE_NAME, patientColumnNames);

        JSONArray patients = new JSONArray(getData(new URL("https://10.4.20.224:8082/openmrs/ws/rest/v1/patientprofile/all")));
        JSONArray personAttributesList = new JSONObject(getData(new URL("https://10.4.20.224:8082/openmrs/ws/rest/v1/personattributetype?v=custom:(name)"))).getJSONArray("results");

        attributeColumnNames = new String[personAttributesList.length()];

        String selectStatement = constructSelectStatement(personAttributesList);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("patientSearchSelectStatement", selectStatement);
        editor.commit();

        String[] columnNames = new String[personAttributesList.length() + 1];
        System.arraycopy(attributeColumnNames, 0, columnNames, 0, attributeColumnNames.length);
        columnNames[personAttributesList.length()] = DBContract.PATIENT_ATTRIBUTES.COLUMN_PATIENT_ID;
        mDbHelper.createTable(db, DBContract.PATIENT_ATTRIBUTES.TABLE_NAME, columnNames);

        insertPatientData(db, patients);
    }


    @JavascriptInterface
    public String getPatient(String uuid){
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * from " + DBContract.PATIENT.TABLE_NAME +
                               " WHERE " + DBContract.PATIENT.COLUMN_PATIENT_UUID + " = '" + uuid + "' limit 1 ", new String[]{});
        c.moveToFirst();
        return c.getString(10);
    }

    @JavascriptInterface
    public String search(String request) throws JSONException, IOException {
        JSONObject paramsJson = new JSONObject(request);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        JSONObject params = paramsJson.getJSONObject("params");
        String name = null;

        if(params.has("q")){
            name = params.getString("q");
        }
        String identifier = null;
        if(params.has("identifier")){
            identifier = params.getString("identifier");
        }
        String customAttributeList = null;
        if(params.has("custom_attribute")){
            customAttributeList = params.getString("custom_attribute");
        }
        String offset = params.getString("startIndex");

        JSONArray attributesToSearch = null;
        if(params.has("patientAttributes")){
            attributesToSearch = params.getJSONArray("patientAttributes");
        }

        String addressFieldName = null;
        if(params.has("address_field_name")){
            addressFieldName = params.getString("address_field_name");
        }

        String addressFieldValue = null;
        if(params.has("address_field_value")){
            addressFieldValue = params.getString("address_field_value");
        }

        Cursor c;

        String sqlString = sharedPref.getString("patientSearchSelectStatement", "");
        sqlString += " FROM " +  DBContract.PATIENT.TABLE_NAME + " p left outer join " +  DBContract.PATIENT_ATTRIBUTES.TABLE_NAME + " pa on p.identifier = pa.identifier ";

        if(addressFieldValue != null && !addressFieldValue.equals("")){
            c = db.rawQuery(sqlString + " WHERE " + addressFieldName + " LIKE '%" + addressFieldValue + "%'", new String[]{});
        }

        else if(!(customAttributeList == null) && !customAttributeList.equals("")){
            String appender = " WHERE ";
            for (int i = 0; i < attributesToSearch.length(); i++) {
                sqlString += appender + attributesToSearch.getString(i) + " LIKE '%" + customAttributeList + "%'";
                appender = " OR ";
            }
            c = db.rawQuery(sqlString, new String[]{});
        }

        else if(identifier != null){
             c = db.rawQuery(sqlString +
                    " WHERE p." + DBContract.PATIENT.COLUMN_PATIENT_ID + " = '" + identifier +
                    "' LIMIT 50 OFFSET " + offset, new String[]{});
        }
        else{
            c = db.rawQuery(sqlString +
                               " WHERE " + DBContract.PATIENT.COLUMN_PATIENT_FIRST_NAME + " LIKE '%" + name +
                               "%' OR " + DBContract.PATIENT.COLUMN_PATIENT_MIDDLE_NAME + " LIKE '%" + name +
                               "%' OR " + DBContract.PATIENT.COLUMN_PATIENT_LAST_NAME + " LIKE '%" + name +
                               "%' LIMIT 50 OFFSET " + offset, new String[]{});
        }
        JSONArray json = constructResponse(attributesToSearch, addressFieldName, c);

        return String.valueOf(new JSONObject().put("pageOfResults", json));
    }

    private JSONArray constructResponse(JSONArray attributesToSearch, String addressFieldName, Cursor c) throws JSONException {
        c.moveToFirst();
        JSONArray json = new JSONArray();
        do {
            JSONObject obj = new JSONObject();
            String[] columnNames = c.getColumnNames();
            JSONObject customAttributes = new JSONObject();
            for (int i = 0; i < columnNames.length; i++) {
                if(attributesToSearch != null) {
                    for (int j = 0; j < attributesToSearch.length(); j++) {
                        if (attributesToSearch.getString(j).equals(columnNames[i])) {
                            customAttributes.put(columnNames[i], c.getString(i));
                            continue;
                        }
                    }
                }
                if(addressFieldName.equals(columnNames[i])){
                    obj.put("addressFieldValue", c.getString(i));
                }
                else {
                    obj.put(columnNames[i], c.getString(i));
                }
            }
            obj.put("customAttribute",  String.valueOf(customAttributes));
            json.put(obj);
        }while(c.moveToNext());
        return json;
    }

    private void insertPatientData(SQLiteDatabase db, JSONArray patients) throws JSONException {
        for (int i = 0; i < patients.length(); i++) {

            ContentValues values = new ContentValues();
            JSONObject patient = new JSONObject(patients.getJSONObject(i).getString("patient"));
            JSONObject person = new JSONObject(patient.getString("person"));
            JSONObject personName = new JSONObject(person.getString("preferredName"));
            String patientIdentifier = new JSONArray(patient.getString("identifiers")).getJSONObject(0).getString("identifier");

            values.put(DBContract.PATIENT.COLUMN_PATIENT_ID, patientIdentifier);
            values.put(DBContract.PATIENT.COLUMN_PATIENT_UUID, patient.getString("uuid"));
            values.put(DBContract.PATIENT.COLUMN_PATIENT_FIRST_NAME, personName.getString("givenName"));
            if(!personName.isNull("middleName"))
                values.put(DBContract.PATIENT.COLUMN_PATIENT_MIDDLE_NAME, personName.getString("middleName"));
            values.put(DBContract.PATIENT.COLUMN_PATIENT_LAST_NAME, personName.getString("familyName"));
            values.put(DBContract.PATIENT.COLUMN_PATIENT_GENDER, person.getString("gender"));
            values.put(DBContract.PATIENT.COLUMN_PATIENT_AGE, person.getString("age"));
            values.put(DBContract.PATIENT.COLUMN_DATE_CREATED, patient.getJSONObject("auditInfo").getString("dateCreated"));
            values.put(DBContract.PATIENT.COLUMN_RURAL_WARD, person.getJSONObject("preferredAddress").getString("address2"));
            values.put(DBContract.PATIENT.COLUMN_PATIENT_JSON, String.valueOf(patients.getJSONObject(i)));
            db.insert(DBContract.PATIENT.TABLE_NAME, null, values);

            values = new ContentValues();

            JSONArray attributes = person.getJSONArray("attributes");
            insertAttributes(db, values, patientIdentifier, attributes);
        }
    }

    private void insertAttributes(SQLiteDatabase db, ContentValues values, String patientIdentifier, JSONArray attributes) throws JSONException {
        if(attributes != null && attributes.length() > 0) {
            for (int j = 0; j < attributes.length(); j++) {
                JSONObject personAttribute = attributes.getJSONObject(j);
                Object object = personAttribute.get("value");
                String value;
                if(object instanceof JSONObject){
                    value = ((JSONObject) object).getString("display");
                }
                else
                    value = String.valueOf(object);
                values.put(personAttribute.getJSONObject("attributeType").getString("display"), value);
            }
            values.put(DBContract.PATIENT_ATTRIBUTES.COLUMN_PATIENT_ID, patientIdentifier);
            db.insert(DBContract.PATIENT_ATTRIBUTES.TABLE_NAME, null, values);
        }
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

    private String constructSelectStatement(JSONArray personAttributesList) throws JSONException {
        String selectStatement = "SELECT ";
        String appender = "p.";
        for (String patientColumnName : patientColumnNames) {
            selectStatement +=  appender + patientColumnName ;
            appender = " , p.";
        }
        appender = " , pa.";

        for(int i = 0; i < personAttributesList.length(); i++){
            attributeColumnNames[i] = personAttributesList.getJSONObject(i).getString("name");
            selectStatement += appender + attributeColumnNames[i];
        }
        return selectStatement;
    }

    private String getData(URL url) throws IOException {
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setHostnameVerifier(hostnameVerifier);              //TODO : Have to fix this
        int responseCode = con.getResponseCode();
        InputStream inputStream = con.getInputStream();
        return convertStreamToString(inputStream);
    }


}
