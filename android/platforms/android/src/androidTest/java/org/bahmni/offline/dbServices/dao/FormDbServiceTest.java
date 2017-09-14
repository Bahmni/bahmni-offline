package org.bahmni.offline.dbServices.dao;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

import net.sqlcipher.database.SQLiteDatabase;

import org.bahmni.offline.Constants;
import org.bahmni.offline.MainActivity;
import org.bahmni.offline.Utils.TestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class FormDbServiceTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private DbHelper mDBHelper;
    private FormDbService formDbService;

    public FormDbServiceTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        mDBHelper = new DbHelper(context, context.getFilesDir() + "/metadata.db", 2);
        mDBHelper.createTable(Constants.CREATE_FORM_TBALE);
        formDbService = new FormDbService(mDBHelper);
        JSONArray forms = new JSONArray(TestUtils.readFileFromAssets("form.json", getInstrumentation().getContext()));
        for (int index = 0; index < forms.length(); index++) {
            formDbService.insertForm(String.valueOf(forms.get(index)));
        }
    }

    @Test
    public void testShouldGetFormForGivenUuid() throws Exception {
        String uuid = "e5e763aa-31df-4931-bed4-0468ddf63aab";
        JSONObject form = new JSONObject(formDbService.getFormByUuid(uuid));
        assertEquals("test_form", form.getString("name"));
        assertEquals(uuid, form.getString("uuid"));
        assertEquals(1, form.getInt("version"));
    }

    @Test
    public void testShouldGiveAllTheFormPresentInDatabase() throws Exception {
        JSONArray forms = new JSONArray(formDbService.getAllForms());
        assertEquals(3, forms.length());
        assertEquals("e5e763aa-31df-4931-bed4-0468ddf63aab", ((JSONObject) forms.get(0)).getString("uuid"));
        assertEquals("80b7273d-eea0-48d0-abae-b3d3bf7e96f1", ((JSONObject) forms.get(1)).getString("uuid"));
        assertEquals("7635fcda-cf1b-4d30-9ea9-595d7d34c7d9", ((JSONObject) forms.get(2)).getString("uuid"));
        assertTrue(((JSONObject) forms.get(0)).isNull("resources"));
        assertTrue(((JSONObject) forms.get(1)).isNull("resources"));
        assertTrue(((JSONObject) forms.get(2)).isNull("resources"));
    }
}