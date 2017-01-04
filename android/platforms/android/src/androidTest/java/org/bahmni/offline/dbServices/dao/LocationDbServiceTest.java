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

public class LocationDbServiceTest extends ActivityInstrumentationTestCase2<MainActivity>{

    public LocationDbServiceTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        super(MainActivity.class);
    }

    @Test
    public void testShouldInsertLocationsAndGetLocationByUuid() throws Exception {

        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        String uuid = "e905bf88-c461-46e7-a2f1-87db4f611f8b";
        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db", 5);
        mDBHelper.createTable(Constants.CREATE_LOGIN_LOCATIONS_TABLE);

        LocationDbService locationDbService = new LocationDbService(mDBHelper);
        String locationsJson = TestUtils.readFileFromAssets("loginLocations.json", getInstrumentation().getContext());

        locationDbService.insertLocations(new JSONObject(locationsJson).getJSONArray("results"));

        JSONObject location = new JSONObject(locationDbService.getLocationByUuid(uuid));

        assertEquals(uuid, location.getJSONObject("value").getString("uuid"));
        assertEquals("IPD", location.getJSONObject("value").getString("display"));
        assertEquals("In patient department", location.getJSONObject("value").getString("description"));

    }
}
