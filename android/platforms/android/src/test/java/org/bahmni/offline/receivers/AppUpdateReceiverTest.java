package org.bahmni.offline.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.bahmni.offline.receivers.AppUpdateReceiver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.bahmni.offline.Constants.APP_UPGRADE_SHARED_PREFERENCE;
import static org.bahmni.offline.Constants.INSTALL_PENDING;
import static org.bahmni.offline.Constants.LAST_DOWNLOADED_APK_NAME;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class AppUpdateReceiverTest {

    @Mock
    Context context;
    @Mock
    Intent intent;
    @Mock
    SharedPreferences mockSharedPreferences;
    @Mock
    SharedPreferences.Editor mockSharedPreferencesEditor;

    AppUpdateReceiver appUpdateReceiver = new AppUpdateReceiver();

    @Test
    public void testRemoveInstallPendingFromSharedPreference() {

        when(context.getSharedPreferences(APP_UPGRADE_SHARED_PREFERENCE, Context.MODE_MULTI_PROCESS)).thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.edit()).thenReturn(mockSharedPreferencesEditor);

        appUpdateReceiver.onReceive(context, intent);

        verify(mockSharedPreferencesEditor).remove(INSTALL_PENDING);
        verify(mockSharedPreferencesEditor).remove(LAST_DOWNLOADED_APK_NAME);
        verify(mockSharedPreferencesEditor).apply();
    }
}
