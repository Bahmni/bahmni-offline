package org.bahmni.offline.db;

import android.content.ContentValues;
import net.sqlcipher.database.SQLiteDatabase;
import org.json.JSONException;
import org.json.JSONObject;

public class AddressService {
    public void insertAddress(SQLiteDatabase db, JSONObject address, String patientUuid) throws JSONException {
        ContentValues values = new ContentValues();

        values.put("address1", address.getString("address1"));
        values.put("address2", address.getString("address2"));
        values.put("address3", address.getString("address3"));
        values.put("address4", address.getString("address4"));
        values.put("address5", address.getString("address5"));
        values.put("address6", address.getString("address6"));
        values.put("cityVillage", address.getString("cityVillage"));
        values.put("stateProvince", address.getString("stateProvince"));
        values.put("postalCode", address.getString("postalCode"));
        values.put("country", address.getString("country"));
        values.put("countyDistrict", address.getString("countyDistrict"));
        values.put("patientUuid", patientUuid);

        db.insertWithOnConflict("patient_address", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
