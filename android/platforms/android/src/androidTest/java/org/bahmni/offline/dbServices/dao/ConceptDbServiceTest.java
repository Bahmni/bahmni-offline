package org.bahmni.offline.dbServices.dao;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.bahmni.offline.MainActivity;
import org.bahmni.offline.Utils.TestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class ConceptDbServiceTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public ConceptDbServiceTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        super(MainActivity.class);
    }

    @Test
    public void testShouldInsertConceptAndUpdateItsSetMembers() throws Exception {
        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_CONCEPT_TABLE);

        ConceptDbService conceptDbService = new ConceptDbService(mDBHelper);
        String conceptJson = TestUtils.readFileFromAssets("concept.json", getInstrumentation().getContext());

        conceptDbService.insertConceptAndUpdateHierarchy(conceptJson, null);

        String conceptUuid = "c36a7537-3f10-11e4-adec-0800271c1b75";
        String expectedConceptName = "Vitals";
        JSONObject concept = new JSONObject(conceptDbService.getConcept(conceptUuid));
        JSONObject actualConcept = concept.getJSONObject("data").getJSONArray("results").getJSONObject(0);
        assertEquals(expectedConceptName, actualConcept.getJSONObject("name").getString("name"));

        String child1Uuid = "c36af094-3f10-11e4-adec-0800271c1b75";
        JSONObject child1Concept = new JSONObject(conceptDbService.getConcept(child1Uuid));
        JSONObject actualChild1Concept = child1Concept.getJSONObject("data").getJSONArray("results").getJSONObject(0);
        String expectedChild1Name = "Pulse Data";
        assertEquals(expectedChild1Name, actualChild1Concept.getJSONObject("name").getString("name"));

        JSONArray actualParents = child1Concept.getJSONObject("parents").getJSONArray("parentConcepts");
        boolean parentPresent = false;
        for (int i = 0; i < actualParents.length(); i++) {
            if (new JSONObject(actualParents.getString(0)).get("uuid").equals(conceptUuid)) {
                parentPresent = true;
            }
        }
        assertTrue(parentPresent);
    }

    @Test
    public void testShouldGetAllParentConceptsForGivenChildConcept() throws Exception {
        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_CONCEPT_TABLE);

        ConceptDbService conceptDbService = new ConceptDbService(mDBHelper);
        String conceptJson = TestUtils.readFileFromAssets("concept.json", getInstrumentation().getContext());

        conceptDbService.insertConceptAndUpdateHierarchy(conceptJson, null);

        String parentConcepts = conceptDbService.getAllParentsInHierarchy("Pulse Data");
        JSONArray results = new JSONArray(parentConcepts);

        assertEquals(2, results.length());
    }

    public void testShouldReturnArrayWithOneConceptNameIfThereIsNoParent() throws Exception {
        Context context = getInstrumentation().getTargetContext();
        SQLiteDatabase.loadLibs(context);

        DbHelper mDBHelper = new DbHelper(context, context.getFilesDir() + "/Bahmni.db");
        mDBHelper.createTable(Constants.CREATE_CONCEPT_TABLE);

        ConceptDbService conceptDbService = new ConceptDbService(mDBHelper);
        String conceptJson = TestUtils.readFileFromAssets("concept.json", getInstrumentation().getContext());

        conceptDbService.insertConceptAndUpdateHierarchy(conceptJson, null);

        String parentConcepts = conceptDbService.getAllParentsInHierarchy("Vitals");
        JSONArray results = new JSONArray(parentConcepts);

        assertEquals(1, results.length());

    }
}

