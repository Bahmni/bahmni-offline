package org.bahmni.offline.dbServices.dao;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

import net.sqlcipher.database.SQLiteConstraintException;
import net.sqlcipher.database.SQLiteDatabase;

import org.bahmni.offline.Constants;
import org.bahmni.offline.MainActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class ErrorLogDbServiceTest  extends ActivityInstrumentationTestCase2<MainActivity> {

    public ErrorLogDbServiceTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        super(MainActivity.class);
    }

    @Test
    public void testShouldStoreErrorInformationOfFailedRequestInErrorLogTable() throws JSONException {
        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db",  5);
        mDBHelper.createTable(Constants.CREATE_ERRORLOG_TABLE);

        ErrorLogDbService errorLogDbService = new ErrorLogDbService();

        errorLogDbService.insertLog("someUuid","http://failedRequest", 500, "Service tempporaily unavailable", null, null, mDBHelper);

        JSONObject errorLog = errorLogDbService.getErrorLogByUuid("someUuid", mDBHelper);

        assertEquals("http://failedRequest", errorLog.get("failedRequest"));
        errorLogDbService.deleteByUuid("someUuid", mDBHelper);

    }

    @Test
    public void testGetErrorLogByUuid() throws Exception {
        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db", 5);
        mDBHelper.createTable(Constants.CREATE_ERRORLOG_TABLE);

        ErrorLogDbService errorLogDbService = new ErrorLogDbService();

        errorLogDbService.insertLog("someUuid1", "http://failedRequest1", 500, "Service tempporaily unavailable", "requestPayload9", "provider9", mDBHelper);

        JSONObject errorLog = errorLogDbService.getErrorLogByUuid("someUuid1", mDBHelper);
        assertEquals("http://failedRequest1", errorLog.get("failedRequest"));
        assertEquals(500, errorLog.get("responseStatus"));
        assertEquals("Service tempporaily unavailable", errorLog.get("stackTrace"));
        assertEquals("requestPayload9", errorLog.get("requestPayload"));
        assertEquals("provider9", errorLog.get("provider"));
        errorLogDbService.deleteByUuid("someUuid1", mDBHelper);

    }

    @Test
    public void testInsertLogWhenUniqueConstraintOnFailedRequestAndPayloadViolated() throws Exception {
        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);
        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db", 5);
        mDBHelper.createTable(Constants.CREATE_ERRORLOG_TABLE);

        ErrorLogDbService errorLogDbService = new ErrorLogDbService();
        errorLogDbService.insertLog("someUuid10", "http://failedRequest10", 500, "Service tempporaily unavailable", "requestPayload9", "provider9", mDBHelper);
        try{
            errorLogDbService.insertLog("someUuid21", "http://failedRequest10", 500, "Service tempporaily unavailable", "requestPayload9", "provider9", mDBHelper);
        }catch (Exception e){
            assertTrue(e instanceof SQLiteConstraintException); //ActivityInstrumentationTestCase2 can't test an expected exception
        }
    }

    @Test
    public void testDeleteLogByUuid() throws JSONException {
        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db", 5);
        mDBHelper.createTable(Constants.CREATE_ERRORLOG_TABLE);

        ErrorLogDbService errorLogDbService = new ErrorLogDbService();

        errorLogDbService.insertLog("someUuid2","http://failedRequest2", 500, "Service tempporaily unavailable", "reqPayload", null, mDBHelper);
        JSONObject errorLog = errorLogDbService.getErrorLogByUuid("someUuid2", mDBHelper);

        assertNotNull(errorLog);
        errorLogDbService.deleteByUuid("someUuid2", mDBHelper);
        JSONObject deletedErrorLog = errorLogDbService.getErrorLogByUuid("someUuid2", mDBHelper);
        assertEquals("Error log should have been deleted",new JSONObject().toString(),deletedErrorLog.toString());
    }
}
