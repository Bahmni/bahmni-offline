package org.bahmni.offline.dbServices.dao;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.bahmni.offline.MainActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class ErrorLogDbServiceTest  extends ActivityInstrumentationTestCase2<MainActivity> {

    public ErrorLogDbServiceTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        super(MainActivity.class);
    }

    @Test
    public void shouldStoreErrorInformationOfFailedRequestInErrorLogTable() throws JSONException {
        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_ERRORLOG_TABLE);

        ErrorLogDbService errorLogDbService = new ErrorLogDbService(mDBHelper);

        errorLogDbService.insertLog("http://failedRequest", 500, "Service tempporaily unavailable");

        JSONArray errorLogList = errorLogDbService.getAllLogs();

        assertEquals("http://failedRequest", errorLogList.getJSONObject(0).getString("failedRequest"));

    }

}
