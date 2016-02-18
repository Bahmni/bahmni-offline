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

public class ConfigDbServiceTest extends ActivityInstrumentationTestCase2<MainActivity>{

    public ConfigDbServiceTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        super(MainActivity.class);
    }

    @Test
    public void testShouldInsertConfig() throws Exception {

        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_CONFIG_TABLE);

        ConfigDbService configDbService = new ConfigDbService(mDBHelper);
        String configJson = TestUtils.readFileFromAssets("config.json", getInstrumentation().getContext());

        String module = "registration";
        String etag = "56e2-97-52bf58d59ae80-gzip";
        String registrationConfig = configDbService.insertConfig(module, configJson, etag);

        JSONObject appJson = new JSONObject(registrationConfig).getJSONObject("value").getJSONObject("app.json");
        assertEquals(etag, new JSONObject(registrationConfig).getString("etag"));
        assertEquals(module, new JSONObject(registrationConfig).getString("module"));
        assertEquals("bahmni.template.registration", appJson.getString("instanceOf"));
        assertEquals("bahmni.registration", appJson.getString("id"));

    }
}
