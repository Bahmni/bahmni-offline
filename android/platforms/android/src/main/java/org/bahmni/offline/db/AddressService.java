package org.bahmni.offline.db;

import android.content.ContentValues;
import net.sqlcipher.database.SQLiteDatabase;
import org.json.JSONException;
import org.json.JSONObject;

public class AddressService {
    public void insertAddress(SQLiteDatabase db, JSONObject address, String[] addressColumnNames, String patientUuid) throws JSONException {
        ContentValues values = new ContentValues();
        for (String addressColumn : addressColumnNames) {
            if (!address.isNull(addressColumn))
                values.put(addressColumn, address.getString(addressColumn));
        }
        values.put("patientUuid", patientUuid);
        db.insert("patient_address", null, values);
    }
}
