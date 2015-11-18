package com.bahmni.offline;

import android.os.Bundle;

import com.ionicframework.android203199.R;
import com.bahmni.offline.js.WebAppInterface;

import org.apache.cordova.CordovaActivity;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkView;

public class MainActivity extends CordovaActivity
{
    private XWalkView xWalkWebView;
    private final static String JS_INTERFACE = "Android";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        xWalkWebView=(XWalkView)findViewById(R.id.xwalkWebView);
        xWalkWebView.addJavascriptInterface(new WebAppInterface(MainActivity.this), JS_INTERFACE);
        xWalkWebView.load("https://10.136.20.45:8082/home", null);
        // turn on debugging
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
    }


}

