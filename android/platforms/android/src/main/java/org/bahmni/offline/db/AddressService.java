package org.bahmni.offline.db;

import android.content.ContentValues;
import net.sqlcipher.database.SQLiteDatabase;
import org.json.JSONException;
import org.json.JSONObject;

public class AddressService {
    public void insertAddress(SQLiteDatabase db, JSONObject address, String patientUuid) throws JSONException {
        ContentValues values = new ContentValues();

        String[] addressFields = new String[]{"address1", "address2", "address3", "address4", "address5", "address6",
                                              "cityVillage", "stateProvince", "postalCode", "country", "countyDistrict"};

        for (String addressField : addressFields) {
            if(!address.isNull(addressField)){
                values.put(addressField, address.getString(addressField));
            }
        }

        values.put("patientUuid", patientUuid);

        db.insertWithOnConflict("patient_address", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
