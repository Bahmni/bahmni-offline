package org.bahmni.offline.dbServices.dao;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.test.ActivityInstrumentationTestCase2;

import org.bahmni.offline.MainActivity;
import org.bahmni.offline.services.AppUpdateService;
import org.junit.Before;
import org.junit.Test;

import static org.bahmni.offline.Constants.APP_UPGRADE_SHARED_PREFERENCE;
import static org.bahmni.offline.Constants.DOWNLOAD_REFERNCE;
import static org.bahmni.offline.Constants.INSTALL_PENDING;
import static org.bahmni.offline.Constants.LAST_DOWNLOADED_APK_NAME;

public class AppUpdateServiceTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private AppUpdateService appUpdateService;
    private Context targetContext;

    public AppUpdateServiceTest() {
        super(MainActivity.class);
    }

    @Before
    public void setUp() {
        targetContext = getInstrumentation().getTargetContext();
        appUpdateService = new AppUpdateService(targetContext);

    }

    @Test
    public void testScheduleDownloadIfDownloadHasNotStartedAndInstallOfPreviousDownloadIsNotPending() throws Exception {
        String downloadUrl = "http://www.abcd.com/def.apk";
        SharedPreferences apkDownloadReferencePreference = targetContext.getSharedPreferences(APP_UPGRADE_SHARED_PREFERENCE, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = apkDownloadReferencePreference.edit();
        editor.remove(INSTALL_PENDING);
        editor.remove(LAST_DOWNLOADED_APK_NAME);
        editor.remove(DOWNLOAD_REFERNCE);
        editor.commit();

        appUpdateService.updateApp(downloadUrl);

        long downloadReference = apkDownloadReferencePreference.getLong(DOWNLOAD_REFERNCE, -1);
        assertNotSame(-1, downloadReference);
        DownloadManager downloadManager = (DownloadManager) targetContext.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadReference);
        Cursor c = downloadManager.query(query);
        c.moveToFirst();
        assertEquals("Bahmni Download", c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE)));
    }

}
