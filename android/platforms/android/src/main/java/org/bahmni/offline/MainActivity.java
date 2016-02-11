package org.bahmni.offline;

import android.os.Bundle;
import org.apache.cordova.CordovaActivity;
import org.bahmni.offline.services.DbService;
import org.xwalk.core.XWalkCookieManager;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkView;

public class MainActivity extends CordovaActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        XWalkView xWalkWebView = (XWalkView) findViewById(R.id.xwalkWebView);
        XWalkCookieManager mCookieManager = new XWalkCookieManager();
        mCookieManager.setAcceptCookie(true);
        mCookieManager.setAcceptFileSchemeCookies(true);
        xWalkWebView.addJavascriptInterface(new DbService(MainActivity.this), "AndroidOfflineService");
        xWalkWebView.loadAppFromManifest("file:///android_asset/manifest.json", null);
        // turn on debugging
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
    }
}
