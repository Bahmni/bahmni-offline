package com.bahmni.offline;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;
import com.bahmni.offline.db.DbHelper;
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
import java.net.PasswordAuthentication;
import java.net.URL;

public class WebAppInterface {
    Context mContext;
    private DbHelper mDBHelper;
    String key = "key";

    WebAppInterface(Context c) {
        mContext = c;
        mDBHelper = new DbHelper(c);
    }

    HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * Show a toast from the web page
     */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface

    public void populateData(String host) throws IOException, JSONException {
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("admin", "test".toCharArray());
            }
        });

//        SQLiteDatabase.loadLibs(mContext);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        String[] patientColumnNames = {
                "identifier",
                "uuid",
                "givenName",
                "middleName",
                "familyName",
                "gender",
                "age",
                "dateCreated",
                "patientJson"};
        String[] attributeTypeColumnNames = {
                "attributeTypeId",
                "attributeName"
        };
        String[] attributeColumnNames = {
                "attributeTypeId",
                "attributeValue",
                "patientId"
        };

        mDBHelper.createTable(db, "patient_attribute_types", attributeTypeColumnNames);
        mDBHelper.createTable(db, "patient", patientColumnNames);
        mDBHelper.createTable(db, "patient_attributes", attributeColumnNames);
        String[] addressColumnNames = getAddressColumns();
        mDBHelper.createTable(db, "patient_address", addressColumnNames);

        insertAttributeTypes(db);

        JSONArray patients;
        int startIndex = 0;
        int pageSize = 1;
        do{
            patients = new JSONObject(getData(new URL("https://10.4.23.4:8082/openmrs/ws/rest/v1/bahmnicore/patientData?startIndex=" + startIndex + "&limit=" + pageSize))).getJSONArray("pageOfResults");
            insertPatientData(db, patients, addressColumnNames);
            startIndex++;
        } while (patients.length() == pageSize);

        createIndices(db);
    }

    private void createIndices(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX givenNameIndex ON patient(givenName)");
        db.execSQL("CREATE INDEX middleNameIndex ON patient(middleName)");
        db.execSQL("CREATE INDEX familyNameIndex ON patient(familyName)");
        db.execSQL("CREATE INDEX identifierIndex ON patient(identifier)");
    }

    private String[] getAddressColumns() throws IOException, JSONException {
        JSONArray addressHierarchyFields = new JSONArray(getData(new URL("https://10.4.23.4:8082/openmrs/module/addresshierarchy/ajax/getOrderedAddressHierarchyLevels.form")));
        String[] addressColumnNames = new String[addressHierarchyFields.length() + 1];
        for (int i = 0; i < addressHierarchyFields.length(); i++) {
            addressColumnNames[i] = addressHierarchyFields.getJSONObject(i).getString("addressField");
        }
        addressColumnNames[addressHierarchyFields.length()] = "patientId";
        return addressColumnNames;
    }

    private void insertAttributeTypes(SQLiteDatabase db) throws JSONException, IOException {
        JSONArray personAttributeTypeList = new JSONObject(getData(new URL("https://10.4.23.4:8082/openmrs/ws/rest/v1/personattributetype?v=custom:(name)"))).getJSONArray("results");
        for (int i = 0; i < personAttributeTypeList.length(); i++) {
            ContentValues values =new ContentValues();
            values.put("attributeTypeId", String.valueOf(i));
            values.put("attributeName", personAttributeTypeList.getJSONObject(i).getString("name"));
            db.insert("patient_attribute_types", null, values);
        }
    }


    @JavascriptInterface
    public String getPatient(String uuid) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * from patient" +
                " WHERE uuid = '" + uuid + "' limit 1 ", new String[]{});
        c.moveToFirst();
        return c.getString(c.getColumnIndex("patientJson"));
    }

    private String getNameSearchCondition(String[] nameParts) {
        final String BY_NAME_PARTS = " (coalesce(givenName" +
                                        ", '') || coalesce(middleName" +
                                        ", '') || coalesce(familyName, '')) like ";
        if (nameParts.length == 0)
            return "";
        else {
            String queryByNameParts = "";
            for (String part : nameParts) {
                if (!queryByNameParts.equals("")) {
                    queryByNameParts += " and " + BY_NAME_PARTS + " '%" + part + "%'";
                } else {
                    queryByNameParts += BY_NAME_PARTS + " '%" + part + "%'";
                }
            }
            return queryByNameParts;
        }
    }

    @JavascriptInterface
    public String search(String request) throws JSONException, IOException {
        JSONObject paramsJson = new JSONObject(request);
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        JSONObject params = paramsJson.getJSONObject("params");
        String[] nameParts = new String[]{};

        if (params.has("q")) {
            String name = params.getString("q");
            nameParts = name.split(" ");
        }
        String identifier = null;
        if (params.has("identifier")) {
            identifier = params.getString("identifier");
        }
        String customAttribute = "";
        if (params.has("custom_attribute")) {
            customAttribute = params.getString("custom_attribute");
        }
        String offset = params.getString("startIndex");

        String attributeNames = "";
        if (params.has("patientAttributes")) {
            JSONArray attributesToSearch = params.getJSONArray("patientAttributes");
            for (int i = 0; i < attributesToSearch.length(); i++) {
                if(i == 0) {
                    attributeNames = "'" + attributesToSearch.getString(i) ;
                }
                else
                    attributeNames += "','" + attributesToSearch.getString(i);
            }
            attributeNames += "'";
        }

        String addressFieldName = null;
        if (params.has("address_field_name")) {
            addressFieldName = params.getString("address_field_name");
            addressFieldName = addressFieldName.replaceAll("_", "");
        }

        String addressFieldValue = null;
        if (params.has("address_field_value")) {
            addressFieldValue = params.getString("address_field_value");
        }



        Cursor c;

        String sqlString = "SELECT identifier, givenName, middleName, familyName, dateCreated, age, gender, uuid, " + addressFieldName +" as addressFieldValue "  +
                            ", '{' || group_concat(DISTINCT (coalesce('\"' || pat.attributeName ||'\":\"' || pa1.attributeValue || '\"' , null))) || '}' as customAttribute" +
                            "  from patient p " +
                            " join patient_address padd " +
                            " on p._id = padd.patientId" +
                            " left outer join patient_attributes pa on p._id = pa.patientId" +
                            " and pa.attributeTypeId in (" +
                            "select "+ "attributeTypeId from patient_attribute_types" +
                            " where attributeName in (" + attributeNames + "))" +
                            " left outer join "+ "patient_attributes pa1 on " +
                            " pa1.patientId = p._id" +
                            " left outer join patient_attribute_types" +
                            " pat on pa1.attributeTypeId = pat.attributeTypeId and pat.attributeName in (" + attributeNames + ")";
        String appender = " WHERE ";

        if (addressFieldValue != null && !addressFieldValue.equals("")) {
            sqlString += appender + "(padd." + addressFieldName + " LIKE '%" + addressFieldValue + "%') ";
            appender = " AND ";
        }
        if (!(customAttribute == null) && !customAttribute.equals("")) {
            sqlString += appender + "pa.attributeValue LIKE '%" + customAttribute + "%'";
            appender = " AND ";

        }
        if (identifier != null) {
            sqlString += appender + " ( p.identifier = '" + identifier + "')";
            appender = " AND ";
        }
        if(nameParts.length >= 1){
            sqlString += appender + getNameSearchCondition(nameParts);
        }
        sqlString += " GROUP BY identifier ORDER BY dateCreated LIMIT 50 OFFSET " + offset;
        c = db.rawQuery(sqlString, new String[]{});
        JSONArray json = constructResponse(c);

        return String.valueOf(new JSONObject().put("pageOfResults", json));
    }

        private JSONArray constructResponse(Cursor c) throws JSONException {
            c.moveToFirst();
            String[] columnNames = c.getColumnNames();
            JSONArray json = new JSONArray();
            while ((!c.isAfterLast())) {
                JSONObject obj = new JSONObject();
                for (int i = 0; i < columnNames.length; i++) {
                    obj.put(columnNames[i], c.getString(i));
                }
                json.put(obj);
                c.moveToNext();
            }
            return json;
        }

    private void insertPatientData(SQLiteDatabase db, JSONArray patients, String[] addressColumnNames) throws JSONException {
        for (int i = 0; i < patients.length(); i++) {

            ContentValues values = new ContentValues();
            JSONObject patient = new JSONObject(patients.getJSONObject(i).getString("patient"));
            JSONObject person = new JSONObject(patient.getString("person"));
            JSONObject personName = new JSONObject(person.getString("preferredName"));
            String patientIdentifier = new JSONArray(patient.getString("identifiers")).getJSONObject(0).getString("identifier");

            values.put("identifier", patientIdentifier);
            values.put("uuid", patient.getString("uuid"));
            values.put("givenName", personName.getString("givenName"));
            if (!personName.isNull("middleName"))
                values.put("middleName", personName.getString("middleName"));
            values.put("familyName", personName.getString("familyName"));
            values.put("gender", person.getString("gender"));
            values.put("age", person.getString("age"));
            values.put("dateCreated", patient.getJSONObject("auditInfo").getString("dateCreated"));
            values.put("patientJson", String.valueOf(patients.getJSONObject(i)));
            db.insert("patient", null, values);

            Cursor d = db.rawQuery("SELECT _id FROM patient"
                    + " WHERE identifier = '" + patientIdentifier + "' LIMIT 1", new String[]{});
            d.moveToFirst();
            int patientId = d.getInt(d.getColumnIndex("_id"));
            d.close();

            JSONArray attributes = person.getJSONArray("attributes");
            insertAttributes(db, patientId, attributes);

            JSONObject address = person.getJSONObject("preferredAddress");
            insertAddress(db, address, addressColumnNames, patientId);
        }
    }

    private void insertAddress(SQLiteDatabase db, JSONObject address, String[] addressColumnNames, int patientId) throws JSONException {
        ContentValues values = new ContentValues();
        for (String addressColumn : addressColumnNames) {
            if(!address.isNull(addressColumn))
                values.put(addressColumn, address.getString(addressColumn));
        }
        values.put("patientId", patientId);
        db.insert("patient_address", null, values);
    }

    private void insertAttributes(SQLiteDatabase db, int patientId, JSONArray attributes) throws JSONException {
        if (attributes != null && attributes.length() > 0) {
            for (int j = 0; j < attributes.length(); j++) {
                ContentValues values = new ContentValues();
                JSONObject personAttribute = attributes.getJSONObject(j);
                Object object = personAttribute.get("value");
                String value;
                if (object instanceof JSONObject) {
                    value = ((JSONObject) object).getString("display");
                } else
                    value = String.valueOf(object);
                String attributeTypeName = personAttribute.getJSONObject("attributeType").getString("display");

                Cursor c = db.rawQuery("SELECT attributeTypeId FROM patient_attribute_types"
                                        + " WHERE attributeName = '" + attributeTypeName + "' LIMIT 1", new String[]{});
                c.moveToFirst();

                String attributeTypeId = c.getString(c.getColumnIndex("attributeTypeId"));
                values.put("attributeTypeId", attributeTypeId);
                values.put("attributeValue", value);
                values.put("patientId", patientId);
                db.insert("patient_attributes", null, values);
            }
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

    private String getData(URL url) throws IOException {
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setHostnameVerifier(hostnameVerifier);              //TODO : Have to fix this
        InputStream inputStream = con.getInputStream();
        return convertStreamToString(inputStream);
    }


}
