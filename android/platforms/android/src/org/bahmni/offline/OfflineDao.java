package org.bahmni.offline;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import net.danlew.android.joda.JodaTimeAndroid;
import net.sqlcipher.database.SQLiteDatabase;

import org.bahmni.offline.db.AddressDao;
import org.bahmni.offline.db.AttributeDao;
import org.bahmni.offline.db.DbHelper;
import org.bahmni.offline.db.MarkerDao;
import org.bahmni.offline.db.PatientDao;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.JavascriptInterface;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class OfflineDao {
    Context mContext;
    private DbHelper mDBHelper;
    private PatientDao patientDao;
    private AddressDao addressDao;
    private AttributeDao attributeDao;
    private MarkerDao markerDao;

    OfflineDao(Context c) {
        mContext = c;
        mDBHelper = new DbHelper(c, c.getExternalFilesDir(null) + "/Bahmni.db");
        JodaTimeAndroid.init(c);
        SQLiteDatabase.loadLibs(mContext);
        patientDao = new PatientDao(mDBHelper);
        addressDao = new AddressDao();
        attributeDao = new AttributeDao();
        markerDao = new MarkerDao(mDBHelper);
    }


    @JavascriptInterface
    public void populateData(String host) {
        new DownloadPatientDataTask(host, 0).execute();
    }

    @JavascriptInterface
    public String getPatientByUuid(String uuid) throws JSONException {
        return String.valueOf(patientDao.getPatientByUuid(uuid));
    }

    @JavascriptInterface
    public String search(String sqlString) throws JSONException, IOException, ExecutionException, InterruptedException {
        JSONArray json = new OfflineSearch(mContext, mDBHelper).execute(sqlString).get();
        return String.valueOf(new JSONObject().put("pageOfResults", json));
    }

    @JavascriptInterface
    public void deletePatientData(String patientIdentifier) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
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
        JSONObject result = new JSONObject();
        result.put("data", new JSONObject().put("pageOfResults", new JSONArray().put(patientDao.getPatientByIdentifier(identifier))));
        return String.valueOf(result);
    }

    @JavascriptInterface
    public String createPatient(String request, String host) throws JSONException, IOException, ExecutionException, InterruptedException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);

        insertPatientData(db, request, Util.getAddressColumns(host), "POST");

        String uuid = new JSONObject(request).getJSONObject("patient").getString("uuid");
        return String.valueOf(new JSONObject().put("data", new JSONObject(getPatientByUuid(uuid))));
    }

    @JavascriptInterface
    public String generateOfflineIdentifier() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("data", "TMP-" + patientDao.generateIdentifier());
        return String.valueOf(result);
    }

    @JavascriptInterface
    public String insertMarker(String eventUuid, String catchmentNumber) {
        return markerDao.insertMarker(eventUuid, catchmentNumber);
    }

    @JavascriptInterface
    public JSONObject getMarker() throws JSONException {
        return markerDao.getMarker();
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

        SQLiteDatabase db = mDBHelper.getWritableDatabase(Constants.KEY);

        mDBHelper.createTable(db, "patient_attribute_types", Constants.ATTRIBUTE_TYPE_COLUMN_NAMES);
        mDBHelper.createTable(db, "patient", Constants.PATIENT_COLUMN_NAMES);
        mDBHelper.createTable(db, "patient_attributes", Constants.ATTRIBUTE_COLUMN_NAMES);
        mDBHelper.createTable(db, "event_log_marker", Constants.EVENT_LOG_MARKER_COLUMN_NAMES);
        mDBHelper.createTable(db, "address_hierarchy_entry", Constants.ADDRESS_HIERARCHY_ENTRY_COLUMN_NAMES);
        mDBHelper.createTable(db, "address_hierarchy_level", Constants.ADDRESS_HIERARCHY_LEVEL_COLUMN_NAMES);
        String[] addressColumnNames = Util.getAddressColumns(host);
        mDBHelper.createTable(db, "patient_address", addressColumnNames);
        mDBHelper.createIdgenTable(db, "idgen", "identifier");
        createIndices(db);

        return db;
    }

    private void insertPatientData(SQLiteDatabase db, String patientObject, String[] addressColumnNames, String requestType) throws JSONException {

        int patientId = patientDao.insertPatient(db, patientObject);

        JSONObject person = new JSONObject(patientObject).getJSONObject("patient").getJSONObject("person");
        JSONArray attributes = person.getJSONArray("attributes");

        attributeDao.insertAttributes(db, patientId, attributes, requestType);

        if (!person.isNull("preferredAddress")) {
            JSONObject address = person.getJSONObject("preferredAddress");
            addressDao.insertAddress(db, address, addressColumnNames, patientId);
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
                String[] addressColumnNames = Util.getAddressColumns(host);
                db = initSchema(host);
                attributeDao.insertAttributeTypes(host, db);
                do {
                    patients = new JSONObject(Util.getData(new URL(host + "/openmrs/ws/rest/v1/bahmnicore/patientData?startIndex=" + startIndex + "&limit=" + pageSize))).getJSONArray("pageOfResults");
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
}
