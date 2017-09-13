package org.bahmni.offline.dbServices.dao;

import android.content.ContentValues;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.JavascriptInterface;

public class FormDbService {
    private DbHelper mDBHelper;

    public FormDbService(DbHelper mDBHelper) {
        this.mDBHelper = mDBHelper;
    }

    @JavascriptInterface
    public void insertForm(String formData) throws JSONException {
        JSONObject form = new JSONObject(formData);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("uuid", form.getString("uuid"));
        values.put("name", form.getString("name"));
        values.put("version", form.getString("version"));
        values.put("resources", form.getString("resources"));

        db.insertWithOnConflict("form", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
}

