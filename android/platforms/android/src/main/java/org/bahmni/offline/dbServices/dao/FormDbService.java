package org.bahmni.offline.dbServices.dao;

import android.content.ContentValues;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONArray;
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

    @JavascriptInterface
    public String getFormByUuid(String uuid) throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * from form" +
                " WHERE uuid = '" + uuid + "'", new String[]{});
        if(c.getCount() < 1){
            c.close();
            return null;
        }
        c.moveToFirst();
        JSONObject form = new JSONObject();
        form.put("name", c.getString(c.getColumnIndex("name")));
        form.put("version", c.getString(c.getColumnIndex("version")));
        form.put("uuid", c.getString(c.getColumnIndex("uuid")));
        form.put("resources", new JSONArray(c.getString(c.getColumnIndex("resources"))));
        c.close();
        return String.valueOf(form);
    }

    @JavascriptInterface
    public String getAllForms() throws JSONException {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        JSONArray formList = new JSONArray();
        Cursor c = db.rawQuery("SELECT * from form", new String[]{});
        if (c.getCount() < 1) {
            c.close();
            return String.valueOf(formList);
        }
        c.moveToFirst();
        for(Integer i=0; i < c.getCount(); i++){
            JSONObject form = new JSONObject();
            form.put("name", c.getString(c.getColumnIndex("name")));
            form.put("version", Integer.parseInt(c.getString(c.getColumnIndex("version"))));
            form.put("uuid", c.getString(c.getColumnIndex("uuid")));
            formList.put(i, form);
            c.moveToNext();
        }
        c.close();
        return String.valueOf(formList);
    }
}

