//This file exists to allow cordova to build the project correctly with this package structure which is not cordova default
//After this issue is fixed this file can be deleted



//package org.bahmni.offline;
//
//import android.os.Bundle;
//
//import org.apache.cordova.CordovaActivity;
//import org.xwalk.core.XWalkPreferences;
//import org.xwalk.core.XWalkView;
//
//public class MainActivity extends CordovaActivity
//{
//    @Override
//    public void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        XWalkView xWalkWebView = (XWalkView) findViewById(R.id.xwalkWebView);
//        xWalkWebView.addJavascriptInterface(new DbService(MainActivity.this), "AndroidOfflineService");
//        xWalkWebView.load("file:///android_asset/www/index.html", null);
//        // turn on debugging
//        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
//    }
//}
