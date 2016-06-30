package org.bahmni.offline;

import android.os.Bundle;

import net.danlew.android.joda.JodaTimeAndroid;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.cordova.CordovaActivity;
import org.bahmni.offline.dbServices.dao.ConceptDbService;
import org.bahmni.offline.dbServices.dao.ConfigDbService;
import org.bahmni.offline.dbServices.dao.DbHelper;
import org.bahmni.offline.dbServices.dao.LocationDbService;
import org.bahmni.offline.dbServices.dao.ReferenceDataDbService;
import org.bahmni.offline.services.AppUpdateService;
import org.bahmni.offline.services.DbService;
import org.xwalk.core.XWalkCookieManager;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkView;

public class MainActivity extends CordovaActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        XWalkView xWalkWebView = (XWalkView) findViewById(R.id.xwalkWebView);
        XWalkCookieManager mCookieManager = new XWalkCookieManager();
        mCookieManager.setAcceptCookie(true);
        mCookieManager.setAcceptFileSchemeCookies(true);

        String databaseName = "/Bahmni.db";
        String dbPath = getExternalFilesDir(null) + databaseName;
        DbHelper mDBHelper = new DbHelper(this, dbPath);

        JodaTimeAndroid.init(this);
        SQLiteDatabase.loadLibs(this);

        xWalkWebView.addJavascriptInterface(new DbService(mDBHelper), "AndroidOfflineService");
        xWalkWebView.addJavascriptInterface(new ConfigDbService(mDBHelper), "AndroidConfigDbService");
        xWalkWebView.addJavascriptInterface(new LocationDbService(mDBHelper), "AndroidLocationDbService");
        xWalkWebView.addJavascriptInterface(new ReferenceDataDbService(mDBHelper), "AndroidReferenceDataDbService");
        xWalkWebView.addJavascriptInterface(new ConceptDbService(mDBHelper), "AndroidConceptDbService");
        xWalkWebView.addJavascriptInterface(new AppUpdateService(this), "AppUpdateService");

        xWalkWebView.loadAppFromManifest("file:///android_asset/manifest.json", null);
        // turn on debugging
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
    }
}
