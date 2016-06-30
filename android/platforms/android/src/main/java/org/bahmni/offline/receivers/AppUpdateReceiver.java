package org.bahmni.offline.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import static org.bahmni.offline.Constants.APP_UPGRADE_SHARED_PREFERENCE;
import static org.bahmni.offline.Constants.INSTALL_PENDING;
import static org.bahmni.offline.Constants.LAST_DOWNLOADED_APK_NAME;

public class AppUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("org.bahmni.offline", "Removing installPending, lastDownloadedApkName");
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_UPGRADE_SHARED_PREFERENCE, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(INSTALL_PENDING);
        editor.remove(LAST_DOWNLOADED_APK_NAME);
        editor.apply();
    }
}
