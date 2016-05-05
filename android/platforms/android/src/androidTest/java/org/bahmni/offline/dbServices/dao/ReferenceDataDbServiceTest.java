package org.bahmni.offline.dbServices.dao;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.bahmni.offline.MainActivity;
import org.bahmni.offline.Utils.TestUtils;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class ReferenceDataDbServiceTest extends ActivityInstrumentationTestCase2<MainActivity>{

    public ReferenceDataDbServiceTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        super(MainActivity.class);
    }

    @Test
    public void testShouldInsertReferenceDataAndGetByKey() throws Exception {

        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        String referenceDataKey = "LoginLocations";
        String etag = "someEtag";

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_REFERENCE_DATA_TABLE);

        ReferenceDataDbService referenceDataDbService = new ReferenceDataDbService(mDBHelper);
        String locationsJson = TestUtils.readFileFromAssets("loginLocations.json", getInstrumentation().getContext());

        referenceDataDbService.insertReferenceData(referenceDataKey, locationsJson, etag);

        JSONObject referenceData = new JSONObject(referenceDataDbService.getReferenceData(referenceDataKey));

        assertEquals(etag, referenceData.getString("etag"));
        assertEquals(referenceDataKey, referenceData.getString("key"));
        assertEquals(new JSONObject(locationsJson).getJSONArray("results").length(), referenceData.getJSONObject("data").getJSONArray("results").length());

    }

    @Test
    public void testShouldInsertEmptyReferenceDataAndGetByKey() throws Exception {

        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        String referenceDataKey = "RelationshipTypeMap";
        String etag = "someEtag";

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_REFERENCE_DATA_TABLE);

        ReferenceDataDbService referenceDataDbService = new ReferenceDataDbService(mDBHelper);

        referenceDataDbService.insertReferenceData(referenceDataKey, "", etag);

        JSONObject referenceData = new JSONObject(referenceDataDbService.getReferenceData(referenceDataKey));

        assertEquals(etag, referenceData.getString("etag"));
        assertEquals(referenceDataKey, referenceData.getString("key"));
        assertEquals("", referenceData.getString("data"));

    }
}
