package org.bahmni.offline.dbServices.dao;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import net.sqlcipher.database.SQLiteDatabase;
import org.bahmni.offline.Constants;
import org.joda.time.DateTime;
import org.joda.time.Years;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class SearchDbService extends AsyncTask<String, Integer, JSONArray> {

    private DbHelper mDBHelper;

    public SearchDbService(DbHelper mDBHelper) {
        this.mDBHelper = mDBHelper;
    }

    @Override
    protected JSONArray doInBackground(String... params) {
        JSONArray json = null;

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        try {
            json = constructResponse(db.rawQuery(generateQuery(params[0]), new String[]{}), params[0]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private JSONArray constructResponse(Cursor c, String parameters) throws JSONException {
        JSONObject params = new JSONObject(parameters);
        c.moveToFirst();
        String[] columnNames = c.getColumnNames();
        JSONArray json = new JSONArray();
        while ((!c.isAfterLast())) {
            JSONObject obj = new JSONObject();
            for (int i = 0; i < columnNames.length; i++) {
                if (columnNames[i].equals("birthdate")) {
                    obj.put("age", Years.yearsBetween(DateTime.parse(c.getString(i)), new DateTime()).getYears());
                } else if(params.has("addressFieldName") && columnNames[i].equals(params.getString("addressFieldName"))){
                    JSONObject address = new JSONObject();
                    address.put(params.getString("addressFieldName"), c.getString(i));
                    obj.put("addressFieldValue", address);
                } else if(params.has("extraIdentifiers") && columnNames[i].equals("extraIdentifiers")){
                    JSONObject extraIdentifiers = new JSONObject();
                    extraIdentifiers.put("extraIdentifiers", c.getString(i));
                    obj.put("addressFieldValue", extraIdentifiers);
                } else{
                    obj.put(columnNames[i], c.getString(i));
                }
            }
            json.put(obj);
            c.moveToNext();
        }
        c.close();
        return json;
    }

    private String generateQuery(String parameters) throws JSONException {
        JSONObject params = new JSONObject(parameters);
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
        if (params.has("addressFieldName") && null != params.getString("addressFieldName")) {
            addressFieldName = params.getString("addressFieldName").replace("_", "");
        }

        String sqlString = "SELECT pi.primaryIdentifier as identifier, pi.extraIdentifiers, givenName, middleName, familyName, dateCreated, birthDate, gender, p.uuid, "  + addressFieldName +
        ", '{' || group_concat(DISTINCT (coalesce('\"' || pat.attributeName ||'\":\"' || pa1.attributeValue || '\"' , null))) || '}' as customAttribute" +
                "  from patient p " +
                "  join patient_identifier pi on p.uuid = pi.patientUuid" +
                " join patient_address padd   on p.uuid = padd.patientUuid" +
                " left outer join patient_attributes pa on p.uuid = pa.patientUuid" +
                " and pa.attributeTypeId in (" +
                "select " + "attributeTypeId from patient_attribute_types" +
                " where attributeName in (" + attributeNames + "))" +
                " left outer join " + "patient_attributes pa1 on " +
                " pa1.patientUuid = p.uuid" +
                " left outer join patient_attribute_types" +
                " pat on pa1.attributeTypeId = pat.attributeTypeId and pat.attributeName in (" + attributeNames + ") " +
                " left outer join encounter on encounter.patientUuid = p.uuid";
        String appender = " WHERE ";

        if (params.has("addressFieldValue") && !params.getString("addressFieldValue").equals("")) {
            sqlString += appender + "(padd." + addressFieldName + " LIKE '%" + params.getString("addressFieldValue") + "%') ";
            appender = " AND ";
        }

        if (params.has("duration")) {
            DateTime startDate =  DateTime.now().minusDays(params.getInt("duration"));
            sqlString += appender + " (encounter.encounterDateTime >= '" + startDate + "' OR p.dateCreated >= '" + startDate + "' )";
            appender = " AND ";
        }


        if (params.has("customAttribute") && !params.getString("customAttribute").equals("")) {
            sqlString += appender + "pa.attributeValue LIKE '%" + params.getString("customAttribute") + "%'";
            appender = " AND ";

        }
        if (params.has("identifier") && !params.getString("identifier").equals("")) {
            sqlString += appender + " ( pi.identifier LIKE '%" + params.getString("identifier") + "%')";
            appender = " AND ";
        }
        if (null != nameParts) {
            sqlString += appender + getNameSearchCondition(nameParts);
            appender = " AND ";
        }
        sqlString += appender + "p.voided = 0";
        sqlString += " GROUP BY p.uuid ORDER BY dateCreated DESC LIMIT 50 OFFSET " + params.getString("startIndex");
        return sqlString;
    }

    private String getNameSearchCondition(String[] nameParts) {
        String BY_NAME_PARTS = " (coalesce(givenName" +
                ", '') || coalesce(middleName" +
                ", '') || coalesce(familyName" +
                ", '') || coalesce(pi.identifier, '')) like ";
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
