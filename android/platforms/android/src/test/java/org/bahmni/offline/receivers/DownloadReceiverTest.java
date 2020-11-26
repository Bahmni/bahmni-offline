package org.bahmni.offline.receivers;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.widget.Toast;

import org.bahmni.offline.receivers.DownloadReceiver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.bahmni.offline.Constants.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Toast.class)
public class DownloadReceiverTest {

    public DownloadReceiver downloadReceiver;
    @Mock
    private Context mContext;
    @Mock
    private DownloadManager mockDownloadManager;
    @Mock
    private SharedPreferences mockSharedPreferences;
    @Mock
    private SharedPreferences.Editor mockSharedPreferenceEditor;
    @Mock
    private Intent intent;
    @Mock
    private Cursor cursor;
    @Mock
    private Toast mockToast;

    @Before
    public void setUp() throws Exception {
        downloadReceiver = new DownloadReceiver();
        when(mContext.getSystemService(mContext.DOWNLOAD_SERVICE)).thenReturn(mockDownloadManager);
        when(mContext.getSharedPreferences(APP_UPGRADE_SHARED_PREFERENCE, Context.MODE_MULTI_PROCESS)).thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.edit()).thenReturn(mockSharedPreferenceEditor);
        when(mockDownloadManager.query(any(DownloadManager.Query.class))).thenReturn(cursor);
    }

    @Test
    public void shouldLaunchInstallActivityIfDownloadIsComplete(){
        long referenceId = 300L;
        when(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)).thenReturn(referenceId);
        when(mockSharedPreferences.getLong(DOWNLOAD_REFERNCE, -1)).thenReturn(referenceId);
        when(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)).thenReturn(1);
        when(cursor.getInt(1)).thenReturn(DownloadManager.STATUS_SUCCESSFUL);
        when(cursor.getColumnIndex(DownloadManager.COLUMN_REASON)).thenReturn(2);
        when(cursor.getInt(2)).thenReturn(DownloadManager.STATUS_SUCCESSFUL);
        when(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME)).thenReturn(3);
        String filename = "/downloads/myapp.apk";
        when(cursor.getString(3)).thenReturn(filename);
        PowerMockito.mockStatic(Toast.class);
        when(Toast.makeText(mContext, "STATUS_SUCCESSFUL\nFilename:\n" + filename, Toast.LENGTH_LONG)).thenReturn(mockToast);

        downloadReceiver.onReceive(mContext, intent);

        verify(mockSharedPreferenceEditor).remove(DOWNLOAD_REFERNCE);
        verify(mockSharedPreferenceEditor).apply();
        verify(mContext).startActivity(any(Intent.class));
        verify(mockToast).show();
    }


    @Test
    public void testShouldNotDownloadApkIfDownloadIsAlreadyEnqueued() throws Exception {
        when(mockSharedPreferences.getLong(DOWNLOAD_REFERNCE, -1)).thenReturn(-1l);

        downloadReceiver.onReceive(mContext, intent);

        verify(mockDownloadManager, never()).query(any(DownloadManager.Query.class));
    }
}