package org.bahmni.offline.services;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.bahmni.offline.dbServices.dao.DbHelper;
import org.joda.time.DateTime;
import org.joda.time.Years;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchDbService extends AsyncTask<String, Integer, JSONArray> {

    private Context mContext;
    private DbHelper mDBHelper;

    public SearchDbService(Context mContext, DbHelper mDBHelper) {
        this.mContext = mContext;
        this.mDBHelper = mDBHelper;
    }

    @Override
    protected JSONArray doInBackground(String... params) {
        JSONArray json = null;

        SQLiteDatabase.loadLibs(mContext);
        SQLiteDatabase db = mDBHelper.getReadableDatabase(Constants.KEY);
        try {
            json = constructResponse(db.rawQuery(generateQuery(params[0]), new String[]{}));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private JSONArray constructResponse(Cursor c) throws JSONException {
        c.moveToFirst();
        String[] columnNames = c.getColumnNames();
        JSONArray json = new JSONArray();
        while ((!c.isAfterLast())) {
            JSONObject obj = new JSONObject();
            for (int i = 0; i < columnNames.length; i++) {
                if (columnNames[i].equals("birthdate")) {
                    obj.put("age", Years.yearsBetween(DateTime.parse(c.getString(i)), new DateTime()).getYears());
                } else {
                    obj.put(columnNames[i], c.getString(i));
                }
            }
            json.put(obj);
            c.moveToNext();
        }
        c.close();
        return json;
    }

    private String generateQuery(String params1) throws JSONException {
        JSONObject params = new JSONObject(params1);
        String nameParts[] = null;
        if (params.has("q") && null != params.getString("q")) {
            nameParts = params.getString("q").split(" ");
        }

        JSONArray attributesArray = null;
        if (params.has("patientAttributes") && !params.getString("patientAttributes").equals("")) {
            attributesArray = new JSONArray(params.getString("patientAttributes"));
        }
        String attributeNames = "";
        if (null != attributesArray && attributesArray.length() > 0) {
            for (int index = 0; index < attributesArray.length(); index++) {
                attributeNames += "'" + attributesArray.get(index) + "',";
            }
            attributeNames = attributeNames.substring(0, attributeNames.length() - 1);
        }

        String addressFieldName = null;
        if (params.has("address_field_name") && null != params.getString("address_field_name")) {
            addressFieldName = params.getString("address_field_name").replace("_", "");
        }

        String sqlString = "SELECT identifier, givenName, middleName, familyName, dateCreated, birthDate, gender, p.uuid, " + addressFieldName + " as addressFieldValue " +
                ", '{' || group_concat(DISTINCT (coalesce('\"' || pat.attributeName ||'\":\"' || pa1.attributeValue || '\"' , null))) || '}' as customAttribute" +
                "  from patient p " +
                " join patient_address padd " +
                " on p.uuid = padd.patientUuid" +
                " left outer join patient_attributes pa on p.uuid = pa.patientUuid" +
                " and pa.attributeTypeId in (" +
                "select " + "attributeTypeId from patient_attribute_types" +
                " where attributeName in (" + attributeNames + "))" +
                " left outer join " + "patient_attributes pa1 on " +
                " pa1.patientUuid = p.uuid" +
                " left outer join patient_attribute_types" +
                " pat on pa1.attributeTypeId = pat.attributeTypeId and pat.attributeName in (" + attributeNames + ")";
        String appender = " WHERE ";

        if (params.has("address_field_value") && !params.getString("address_field_value").equals("")) {
            sqlString += appender + "(padd." + addressFieldName + " LIKE '%" + params.getString("address_field_value") + "%') ";
            appender = " AND ";
        }
        if (params.has("custom_attribute") && !params.getString("custom_attribute").equals("")) {
            sqlString += appender + "pa.attributeValue LIKE '%" + params.getString("custom_attribute") + "%'";
            appender = " AND ";

        }
        if (params.has("identifier") && !params.getString("identifier").equals("")) {
            sqlString += appender + " ( p.identifier LIKE '" + params.getString("identifierPrefix") + "%" + params.getString("identifier") + "%')";
            appender = " AND ";
        }
        if (null != nameParts) {
            sqlString += appender + getNameSearchCondition(nameParts);
        }
        sqlString += " GROUP BY p.uuid ORDER BY dateCreated DESC LIMIT 50 OFFSET " + params.getString("startIndex");
        return sqlString;
    }

    private String getNameSearchCondition(String[] nameParts) {
        String BY_NAME_PARTS = " (coalesce(givenName" +
                ", '') || coalesce(middleName" +
                ", '') || coalesce(familyName" +
                ", '') || coalesce(identifier, '')) like ";
        if (nameParts.length == 0)
            return "";
        else {
            String queryByNameParts = "";
            for (int index = 0; index < nameParts.length; index++) {
                if (!queryByNameParts.equals("")) {
                    queryByNameParts += " and " + BY_NAME_PARTS + " '%" + nameParts[index] + "%'";
                } else {
                    queryByNameParts += BY_NAME_PARTS + " '%" + nameParts[index] + "%'";
                }
            }
            return queryByNameParts;
        }
    }

}
