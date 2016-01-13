package main.java.org.bahmni.offline;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;
import net.danlew.android.joda.JodaTimeAndroid;
import net.sqlcipher.database.SQLiteDatabase;
import main.java.org.bahmni.offline.db.DbHelper;
import org.joda.time.DateTime;
import org.joda.time.Years;
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
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class WebAppInterface {
    Context mContext;
    private DbHelper mDBHelper;
    String key = "key";

    WebAppInterface(Context c) {
        mContext = c;
        mDBHelper = new DbHelper(c);
        JodaTimeAndroid.init(c);
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
    public void populateData(String host) {
        new DownloadPatientDataTask(host, 0).execute();
    }

    @JavascriptInterface
    public String getPatient(String uuid) throws JSONException {
        SQLiteDatabase.loadLibs(mContext);
        SQLiteDatabase db = mDBHelper.getReadableDatabase(key);
        Cursor c = db.rawQuery("SELECT * from patient" +
                " WHERE uuid = '" + uuid + "' limit 1 ", new String[]{});
        c.moveToFirst();
        JSONObject result = new JSONObject();
        result.put("patient", new JSONObject(c.getString(c.getColumnIndex("patientJson"))));

        String relationships = c.getString(c.getColumnIndex("relationships"));
        if (relationships != null)
            result.put("relationships", new JSONArray(relationships));
        return String.valueOf(result);
    }

    @JavascriptInterface
    public String search(String sqlString) throws JSONException, IOException, ExecutionException, InterruptedException {
        JSONArray json = new SearchTask().execute(generateQuery(sqlString)).get();
        return String.valueOf(new JSONObject().put("pageOfResults", json));
    }

    @JavascriptInterface
    public void deletePatientData(String patientIdentifier) {
        SQLiteDatabase.loadLibs(mContext);
        SQLiteDatabase db = mDBHelper.getReadableDatabase(key);
        Cursor c = db.rawQuery("SELECT _id from patient" +
                " WHERE identifier = '" + patientIdentifier + "' limit 1 ", new String[]{});
        c.moveToFirst();
        int patientId = c.getInt(c.getColumnIndex("_id"));

        db.beginTransaction();

        db.rawExecSQL("DELETE FROM patient_attributes WHERE patientId = '" + patientId + "'");
        db.rawExecSQL("DELETE FROM patient_address WHERE patientId = '" + patientId + "'");
        db.rawExecSQL("DELETE FROM patient WHERE _id = '" + patientId + "'");

        db.setTransactionSuccessful();

        db.endTransaction();
    }

    @JavascriptInterface
    public String getPatientByIdentifier(String identifier) throws JSONException {
        SQLiteDatabase.loadLibs(mContext);
        SQLiteDatabase db = mDBHelper.getReadableDatabase(key);
        Cursor c = db.rawQuery("SELECT p.identifier, p.givenName, p.familyName, p.gender, p.birthdate, p.uuid from patient p where p.identifier LIKE '%" + identifier + "%' LIMIT 1", new String[]{});
        c.moveToFirst();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("identifier", c.getString(c.getColumnIndex("identifier")));
        jsonObject.put("givenName", c.getString(c.getColumnIndex("givenName")));
        jsonObject.put("familyName", c.getString(c.getColumnIndex("familyName")));
        jsonObject.put("gender", c.getString(c.getColumnIndex("gender")));
        jsonObject.put("birthdate", c.getString(c.getColumnIndex("birthdate")));
        jsonObject.put("uuid", c.getString(c.getColumnIndex("uuid")));
        JSONObject result = new JSONObject();
        result.put("data", new JSONObject().put("pageOfResults", new JSONArray().put(jsonObject)));
        return String.valueOf(result);
    }

    @JavascriptInterface
    public String createPatient(String request, String host) throws JSONException, IOException, ExecutionException, InterruptedException {
        SQLiteDatabase.loadLibs(mContext);
        SQLiteDatabase db = mDBHelper.getReadableDatabase(key);

        insertPatientData(db, request, getAddressColumns(host), "POST");

        String uuid = new JSONObject(request).getJSONObject("patient").getString("uuid");
        return String.valueOf(new JSONObject().put("data", new JSONObject(getPatient(uuid))));
    }

    @JavascriptInterface
    public String generateOfflineIdentifier() throws JSONException {
        SQLiteDatabase.loadLibs(mContext);
        SQLiteDatabase db = mDBHelper.getReadableDatabase(key);

        Cursor c = db.rawQuery("SELECT * from idgen limit 1 ", new String[]{});
        c.moveToFirst();
        JSONObject result = new JSONObject();
        int _id = 1;
        int identifier = 1;

        if (c.getCount() > 0) {
            _id = c.getInt(c.getColumnIndex("_id"));
            identifier = c.getInt(c.getColumnIndex("identifier"));
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("_id", _id);
        contentValues.put("identifier", ++identifier);
        db.insertWithOnConflict("idgen", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);

        result.put("data", "TMP-" + identifier);
        return String.valueOf(result);
    }


    private void createIndices(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX givenNameIndex ON patient(givenName)");
        db.execSQL("CREATE INDEX middleNameIndex ON patient(middleName)");
        db.execSQL("CREATE INDEX familyNameIndex ON patient(familyName)");
        db.execSQL("CREATE INDEX identifierIndex ON patient(identifier)");
    }

    private SQLiteDatabase initSchema(String host) throws IOException, JSONException {
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("admin", "test".toCharArray());
            }
        });

        SQLiteDatabase.loadLibs(mContext);
        SQLiteDatabase db = mDBHelper.getWritableDatabase(key);

        String[] patientColumnNames = {
                "identifier",
                "uuid",
                "givenName",
                "middleName",
                "familyName",
                "gender",
                "birthdate",
                "dateCreated",
                "patientJson",
                "relationships"};
        String[] attributeTypeColumnNames = {
                "attributeTypeId",
                "uuid",
                "attributeName",
                "format"
        };
        String[] attributeColumnNames = {
                "attributeTypeId",
                "attributeValue",
                "patientId"
        };

        mDBHelper.createTable(db, "patient_attribute_types", attributeTypeColumnNames);
        mDBHelper.createTable(db, "patient", patientColumnNames);
        mDBHelper.createTable(db, "patient_attributes", attributeColumnNames);
        String[] addressColumnNames = getAddressColumns(host);
        mDBHelper.createTable(db, "patient_address", addressColumnNames);
        mDBHelper.createIdgenTable(db, "idgen", "identifier");
        createIndices(db);

        return db;
    }

    private String[] getAddressColumns(String host) throws IOException, JSONException {
        JSONArray addressHierarchyFields = new JSONArray(getData(new URL(host + "/openmrs/module/addresshierarchy/ajax/getOrderedAddressHierarchyLevels.form")));
        String[] addressColumnNames = new String[addressHierarchyFields.length() + 1];
        for (int i = 0; i < addressHierarchyFields.length(); i++) {
            addressColumnNames[i] = addressHierarchyFields.getJSONObject(i).getString("addressField");
        }
        addressColumnNames[addressHierarchyFields.length()] = "patientId";
        return addressColumnNames;
    }

    private void insertAttributeTypes(String host, SQLiteDatabase db) throws JSONException, IOException {
        JSONArray personAttributeTypeList = new JSONObject(getData(new URL(host + "/openmrs/ws/rest/v1/personattributetype?v=custom:(name,uuid,format)"))).getJSONArray("results");
        for (int i = 0; i < personAttributeTypeList.length(); i++) {
            ContentValues values = new ContentValues();
            values.put("attributeTypeId", String.valueOf(i));
            values.put("uuid", personAttributeTypeList.getJSONObject(i).getString("uuid"));
            values.put("attributeName", personAttributeTypeList.getJSONObject(i).getString("name"));
            values.put("format", personAttributeTypeList.getJSONObject(i).getString("format"));
            db.insert("patient_attribute_types", null, values);
        }
    }

    private JSONArray constructResponse(Cursor c) throws JSONException {
        c.moveToFirst();
        String[] columnNames = c.getColumnNames();
        JSONArray json = new JSONArray();
        while ((!c.isAfterLast())) {
            JSONObject obj = new JSONObject();
            for (int i = 0; i < columnNames.length; i++) {
                if (columnNames[i].equals("birthdate")) {
                    obj.put("age", Years.yearsBetween(DateTime.parse(c.getString(i)), new DateTime()).getYears());
                } else {
                    obj.put(columnNames[i], c.getString(i));
                }
            }
            json.put(obj);
            c.moveToNext();
        }
        return json;
    }

    private void parseAttributeValues(JSONArray attributes, ArrayList<JSONObject> attributeTypeMap) throws JSONException {
        for (int i = 0; i < attributes.length(); i++) {
            JSONObject attribute = attributes.getJSONObject(i);
            if (attribute.isNull("voided") || (!attribute.isNull("voided") && !attribute.getBoolean("voided"))) {
                String format = getFormat(attributeTypeMap, attribute);
                if ("java.lang.Integer".equals(format)) {
                    attribute.put("value", Integer.parseInt(attribute.getString("value")));
                }
                if ("java.lang.Float".equals(format)) {
                    attribute.put("value", Float.parseFloat(attribute.getString("value")));
                } else if ("java.lang.Boolean".equals(format)) {
                    attribute.put("value", attribute.getString("value").equals("true"));

                } else if ("org.openmrs.Concept".equals(format)) {
                    String display = attribute.getString("value");
                    JSONObject value = new JSONObject();
                    value.put("display", display);
                    value.put("uuid", attribute.getString("hydratedObject"));
                    attribute.put("value", value);
                }
            }
        }
    }

    private String getFormat(ArrayList<JSONObject> attributeTypeMap, JSONObject attribute) throws JSONException {
        for (JSONObject attributeEntry : attributeTypeMap) {
            if (attributeEntry.getString("uuid").equals(attribute.getJSONObject("attributeType").getString("uuid"))) {
                return attributeEntry.getString("format");
            }
        }
        return null;
    }

    private void insertPatientData(SQLiteDatabase db, String patientObject, String[] addressColumnNames, String requestType) throws JSONException {
        JSONObject patientData = new JSONObject(patientObject);
        ContentValues values = new ContentValues();
        JSONObject patient = patientData.getJSONObject("patient");
        JSONObject person = patient.getJSONObject("person");
        JSONObject personName = person.getJSONObject("preferredName");
        String patientIdentifier = new JSONArray(patient.getString("identifiers")).getJSONObject(0).getString("identifier");
        JSONArray attributes = person.getJSONArray("attributes");

        JSONArray relationships = patientData.getJSONArray("relationships");
        if (relationships.length() > 0) {
            for (int i = 0; i < relationships.length(); i++) {
                JSONObject relationship = relationships.getJSONObject(i);
                JSONObject value = new JSONObject();
                value.put("display", personName.getString("givenName") + personName.getString("familyName"));
                value.put("uuid", patient.getString("uuid"));
                relationship.put("personA", value);
            }
        }


        Cursor d = db.rawQuery("SELECT attributeTypeId, uuid, attributeName, format FROM patient_attribute_types", new String[]{});
        d.moveToFirst();
        ArrayList<JSONObject> attributeTypeMap = new ArrayList<JSONObject>();
        while (!d.isAfterLast()) {
            JSONObject attributeEntry = new JSONObject();
            attributeEntry.put("attributeTypeId", d.getInt(d.getColumnIndex("attributeTypeId")));
            attributeEntry.put("uuid", d.getString(d.getColumnIndex("uuid")));
            attributeEntry.put("attributeName", d.getString(d.getColumnIndex("attributeName")));
            attributeEntry.put("format", d.getString(d.getColumnIndex("format")));
            attributeTypeMap.add(attributeEntry);
            d.moveToNext();
        }
        d.close();

        if (requestType.equals("POST")) {
            parseAttributeValues(attributes, attributeTypeMap);
        }

        values.put("identifier", patientIdentifier);
        values.put("uuid", patient.getString("uuid"));
        values.put("givenName", personName.getString("givenName"));
        if (!personName.isNull("middleName"))
            values.put("middleName", personName.getString("middleName"));
        values.put("familyName", personName.getString("familyName"));
        values.put("gender", person.getString("gender"));
        values.put("birthdate", person.getString("birthdate"));
        values.put("dateCreated", person.getJSONObject("auditInfo").getString("dateCreated"));
        values.put("patientJson", String.valueOf(patient));
        if (!patientData.isNull("relationships"))
            values.put("relationships", String.valueOf(patientData.getJSONArray("relationships")));
        db.insert("patient", null, values);

        d = db.rawQuery("SELECT _id FROM patient"
                + " WHERE identifier = '" + patientIdentifier + "' LIMIT 1", new String[]{});
        d.moveToFirst();
        int patientId = d.getInt(d.getColumnIndex("_id"));
        d.close();


        insertAttributes(db, patientId, attributes, attributeTypeMap);

        if (!person.isNull("preferredAddress")) {
            JSONObject address = person.getJSONObject("preferredAddress");
            insertAddress(db, address, addressColumnNames, patientId);
        }
    }

    private void insertAddress(SQLiteDatabase db, JSONObject address, String[] addressColumnNames, int patientId) throws JSONException {
        ContentValues values = new ContentValues();
        for (String addressColumn : addressColumnNames) {
            if (!address.isNull(addressColumn))
                values.put(addressColumn, address.getString(addressColumn));
        }
        values.put("patientId", patientId);
        db.insert("patient_address", null, values);
    }

    private void insertAttributes(SQLiteDatabase db, int patientId, JSONArray attributes, ArrayList<JSONObject> attributeTypeMap) throws JSONException {
        if (attributes != null && attributes.length() > 0) {
            for (int j = 0; j < attributes.length(); j++) {
                ContentValues values = new ContentValues();
                JSONObject personAttribute = attributes.getJSONObject(j);
                if (personAttribute.isNull("voided") || (!personAttribute.isNull("voided") && !personAttribute.getBoolean("voided"))) {
                    Object object = personAttribute.get("value");
                    String value;
                    if (object instanceof JSONObject) {
                        value = ((JSONObject) object).getString("display");
                    } else
                        value = String.valueOf(object);

                    String attributeTypeId = null;

                    for (JSONObject attributeEntry : attributeTypeMap) {
                        if (attributeEntry.getString("uuid").equals(personAttribute.getJSONObject("attributeType").getString("uuid"))) {
                            attributeTypeId = attributeEntry.getString("attributeTypeId");
                        }
                    }

                    values.put("attributeTypeId", attributeTypeId);
                    values.put("attributeValue", value);
                    values.put("patientId", patientId);
                    db.insert("patient_attributes", null, values);
                }
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

    private String generateQuery(String params1) throws JSONException {
        JSONObject params = new JSONObject(params1);
        String nameParts[] = null;
        if (params.has("q") && null != params.getString("q")) {
            nameParts = params.getString("q").split(" ");
        }

        JSONArray attributesArray = null;
        if (params.has("patientAttributes") && !params.getString("patientAttributes").equals("")) {
            attributesArray = new JSONArray(params.getString("patientAttributes"));
        }
        String attributeNames = "";
        if (null != attributesArray && attributesArray.length() > 0) {
            for (int index = 0; index < attributesArray.length(); index++) {
                attributeNames += "'" + attributesArray.get(index) + "',";
            }
            attributeNames = attributeNames.substring(0, attributeNames.length() - 1);
        }

        String addressFieldName = null;
        if (params.has("address_field_name") && null != params.getString("address_field_name")) {
            addressFieldName = params.getString("address_field_name").replace("_", "");
        }

        String sqlString = "SELECT identifier, givenName, middleName, familyName, dateCreated, birthDate, gender, p.uuid, " + addressFieldName + " as addressFieldValue " +
                ", '{' || group_concat(DISTINCT (coalesce('\"' || pat.attributeName ||'\":\"' || pa1.attributeValue || '\"' , null))) || '}' as customAttribute" +
                "  from patient p " +
                " join patient_address padd " +
                " on p._id = padd.patientId" +
                " left outer join patient_attributes pa on p._id = pa.patientId" +
                " and pa.attributeTypeId in (" +
                "select " + "attributeTypeId from patient_attribute_types" +
                " where attributeName in (" + attributeNames + "))" +
                " left outer join " + "patient_attributes pa1 on " +
                " pa1.patientId = p._id" +
                " left outer join patient_attribute_types" +
                " pat on pa1.attributeTypeId = pat.attributeTypeId and pat.attributeName in (" + attributeNames + ")";
        String appender = " WHERE ";

        if (params.has("address_field_value") && !params.getString("address_field_value").equals("")) {
            sqlString += appender + "(padd." + addressFieldName + " LIKE '%" + params.getString("address_field_value") + "%') ";
            appender = " AND ";
        }
        if (params.has("custom_attribute") && !params.getString("custom_attribute").equals("")) {
            sqlString += appender + "pa.attributeValue LIKE '%" + params.getString("custom_attribute") + "%'";
            appender = " AND ";

        }
        if (params.has("identifier") && !params.getString("identifier").equals("")) {
            sqlString += appender + " ( p.identifier = '" + params.getString("identifierPrefix") + params.getString("identifier") + "')";
            appender = " AND ";
        }
        if (null != nameParts) {
            sqlString += appender + getNameSearchCondition(nameParts);
        }
        sqlString += " GROUP BY identifier ORDER BY dateCreated DESC LIMIT 50 OFFSET " + params.getString("startIndex");
        return sqlString;
    }

    private String getNameSearchCondition(String[] nameParts) {
        String BY_NAME_PARTS = " (coalesce(givenName" +
                ", '') || coalesce(middleName" +
                ", '') || coalesce(familyName" +
                ", '') || identifier ) like ";
        if (nameParts.length == 0)
            return "";
        else {
            String queryByNameParts = "";
            for (int index = 0; index < nameParts.length; index++) {
                if (!queryByNameParts.equals("")) {
                    queryByNameParts += " and " + BY_NAME_PARTS + " '%" + nameParts[index] + "%'";
                } else {
                    queryByNameParts += BY_NAME_PARTS + " '%" + nameParts[index] + "%'";
                }
            }
            return queryByNameParts;
        }
    }

    private class DownloadPatientDataTask extends AsyncTask<String, Integer, Integer> {

        private SQLiteDatabase db;
        private String host;
        private int startIndex;
        ProgressDialog progress;

        public DownloadPatientDataTask(String host, int startIndex) {
            this.host = host;
            this.startIndex = startIndex;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(mContext);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setTitle("Syncing Data");
            progress.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            JSONArray patients;
            int pageSize = 1;
            try {
                String[] addressColumnNames = getAddressColumns(host);
                db = initSchema(host);
                insertAttributeTypes(host, db);
                do {
                    patients = new JSONObject(getData(new URL(host + "/openmrs/ws/rest/v1/bahmnicore/patientData?startIndex=" + startIndex + "&limit=" + pageSize))).getJSONArray("pageOfResults");
                    insertPatientData(db, String.valueOf(patients.get(0)), addressColumnNames, "GET");
                    startIndex++;
                } while (patients.length() == pageSize);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return startIndex;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            progress.dismiss();
        }
    }

    private class SearchTask extends AsyncTask<String, Integer, JSONArray> {

        @Override
        protected JSONArray doInBackground(String... params) {
            JSONArray json = null;

            SQLiteDatabase.loadLibs(mContext);
            SQLiteDatabase db = mDBHelper.getReadableDatabase(key);

            Cursor c = db.rawQuery(params[0], new String[]{});
            try {
                json = constructResponse(c);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json;
        }

    }


}
