package org.bahmni.offline;

import android.os.Bundle;

import org.apache.cordova.CordovaActivity;
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
        xWalkWebView.addJavascriptInterface(new OfflineDao(MainActivity.this), "AndroidOfflineDao");
        xWalkWebView.load("file:///android_asset/www/index.html", null);
        // turn on debugging
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
    }
}
