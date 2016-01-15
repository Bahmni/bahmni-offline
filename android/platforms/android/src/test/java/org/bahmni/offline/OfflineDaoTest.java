package org.bahmni.offline;

import android.test.AndroidTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk=18)
public class OfflineDaoTest extends AndroidTestCase {

    @Test
    public void shouldPrint() throws Exception {
        System.out.println("yay");
    }
}
