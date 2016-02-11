package org.bahmni.offline.dbServices.dao;


import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.AddressHierarchyEntry;
import org.bahmni.offline.Constants;
import org.bahmni.offline.MainActivity;
import org.bahmni.offline.Utils.TestUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class AddressHierarchyDbServiceTest extends ActivityInstrumentationTestCase2<MainActivity>{

    private DbHelper mDBHelper;

    public AddressHierarchyDbServiceTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        super(MainActivity.class);
    }

    public void setUp() throws JSONException {
        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        AddressHierarchyDbService addressHierarchyDbService = new AddressHierarchyDbService(mDBHelper);
        mDBHelper.createTable(Constants.CREATE_ADDRESS_HIERARCHY_ENTRY_TABLE);
        mDBHelper.createTable(Constants.CREATE_ADDRESS_HIERARCHY_LEVEL_TABLE);
        String addressHierarchyEntryJson = TestUtils.readFileFromAssets("addressHierarchyEntry.json", getInstrumentation().getContext());

        addressHierarchyDbService.insertAddressHierarchy(new JSONObject(addressHierarchyEntryJson).getJSONObject("parent"));
        addressHierarchyDbService.insertAddressHierarchy(new JSONObject(addressHierarchyEntryJson).getJSONObject("child"));
    }

    @Test
    public void testShouldInsertAddressHierarchyEntryAndLevel() throws Exception {

        String parentAddressUuid = "b3f2af24-ae8f-4699-83d9-78e0d97ba976";
        String childAddressUuid = "559ba00d-d2d6-443e-be7b-f4e9fb7265fb";

        AddressHierarchyEntry parentAddressHierarchyEntry = new AddressHierarchyDbService(mDBHelper).getAddressHierarchyEntryById(1);
        AddressHierarchyEntry childAddressHierarchyEntry = new AddressHierarchyDbService(mDBHelper).getAddressHierarchyEntryById(2);

        assertEquals(parentAddressHierarchyEntry.getName(), "Barisal");
        assertEquals(parentAddressHierarchyEntry.getUuid(), parentAddressUuid);
        assertEquals(childAddressHierarchyEntry.getName(), "Barguna");
        assertEquals(childAddressHierarchyEntry.getUuid(), childAddressUuid);
        assertEquals(childAddressHierarchyEntry.getParent().getName(), "Barisal");

    }


    @Test
    public void testShouldSearchForParentAddress() throws Exception {

        String searchString = "ba";

        final JSONObject params = new JSONObject();
        params.put("searchString", searchString);
        params.put("parentUuid", null);
        params.put("limit", 20);
        params.put("addressField", "stateProvince");

        List<AddressHierarchyEntry> addressHierarchyEntries= new AddressHierarchyDbService(mDBHelper).search(new JSONObject(params.toString()));

        assertEquals(addressHierarchyEntries.size(), 1);
        assertEquals(addressHierarchyEntries.get(0).getName(), "Barisal");
        assertEquals(addressHierarchyEntries.get(0).getUuid(), "b3f2af24-ae8f-4699-83d9-78e0d97ba976");

    }

    @Test
    public void testShouldSearchForChildAddressUsingParentUuid() throws Exception {

        String searchString = "ba";

        final JSONObject params = new JSONObject();
        params.put("searchString", searchString);
        params.put("parentUuid", "b3f2af24-ae8f-4699-83d9-78e0d97ba976");
        params.put("limit", 20);
        params.put("addressField", "countyDistrict");

        List<AddressHierarchyEntry> addressHierarchyEntries= new AddressHierarchyDbService(mDBHelper).search(new JSONObject(params.toString()));

        assertEquals(addressHierarchyEntries.size(), 1);
        assertEquals(addressHierarchyEntries.get(0).getName(), "Barguna");
        assertEquals(addressHierarchyEntries.get(0).getUuid(), "559ba00d-d2d6-443e-be7b-f4e9fb7265fb");
        assertEquals(addressHierarchyEntries.get(0).getParent().getName(), "Barisal");
        assertEquals(addressHierarchyEntries.get(0).getParent().getUuid(), "b3f2af24-ae8f-4699-83d9-78e0d97ba976");

    }
}
