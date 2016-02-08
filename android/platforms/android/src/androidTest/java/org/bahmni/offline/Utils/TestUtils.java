package org.bahmni.offline.Utils;

import android.content.Context;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class TestUtils {

    public static ArrayList<JSONObject> getAttributeTypeMap(SQLiteDatabase db) throws JSONException {
        Cursor d = db.rawQuery("SELECT attributeTypeId, uuid, attributeName, format FROM patient_attribute_types", new String[]{});
        d.moveToFirst();
        ArrayList<JSONObject> attributeTypeMap = new ArrayList<JSONObject>();
        while (!d.isAfterLast()) {
            JSONObject attributeEntry = new JSONObject();
            attributeEntry.put("attributeTypeId", d.getInt(d.getColumnIndex("attributeTypeId")));
            attributeEntry.put("uuid", d.getString(d.getColumnIndex("uuid")));
            attributeEntry.put("attributeName", d.getString(d.getColumnIndex("attributeName")));
            attributeEntry.put("format", d.getString(d.getColumnIndex("format")));
            attributeTypeMap.add(attributeEntry);
            d.moveToNext();
        }
        d.close();
        return attributeTypeMap;
    }

    public static String readFileFromAssets(String fileName, Context c) {
        try {
            InputStream is = c.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String text = new String(buffer);

            return text;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
