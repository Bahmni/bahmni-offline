package org.bahmni.offline.services;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import org.xwalk.core.JavascriptInterface;

import java.io.File;

import static org.bahmni.offline.Constants.APP_UPGRADE_SHARED_PREFERENCE;
import static org.bahmni.offline.Constants.DOWNLOAD_REFERNCE;
import static org.bahmni.offline.Constants.INSTALL_PENDING;
import static org.bahmni.offline.Constants.LAST_DOWNLOADED_APK_NAME;

public class AppUpdateService {


    private final Context context;

    public AppUpdateService(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public String getVersion() throws PackageManager.NameNotFoundException {
        PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        return pInfo.versionName;
    }

    @JavascriptInterface
    public void updateApp(String url) {
        SharedPreferences apkDownloadReferencePreference = context.getSharedPreferences(APP_UPGRADE_SHARED_PREFERENCE, Context.MODE_MULTI_PROCESS);
        long downloadReference = apkDownloadReferencePreference.getLong(DOWNLOAD_REFERNCE, -1);


        if (downloadReference == -1) {
            boolean installPending = apkDownloadReferencePreference.getBoolean(INSTALL_PENDING, false);
            if (installPending) {
                String lastDownloadedFileName = apkDownloadReferencePreference.getString(LAST_DOWNLOADED_APK_NAME, null);
                Intent installIntent = new Intent(Intent.ACTION_VIEW);
                installIntent.setDataAndType(Uri.fromFile(new File(lastDownloadedFileName)),
                        "application/vnd.android.package-archive");
                installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(installIntent);
            } else {
                //start downloading the file using the download manager
                DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                Uri Download_Uri = Uri.parse(url);
                DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
                request.setTitle("Bahmni Download");
                request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "Bahmni.apk");
                downloadReference = downloadManager.enqueue(request);

                SharedPreferences.Editor editor = apkDownloadReferencePreference.edit();
                editor.putLong(DOWNLOAD_REFERNCE, downloadReference);
                editor.apply();
            }
        } else {
            Toast.makeText(context, "App update is already scheduled", Toast.LENGTH_LONG).show();
        }

    }
}
