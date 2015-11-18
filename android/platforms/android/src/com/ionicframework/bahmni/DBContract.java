package com.ionicframework.bahmni;

import android.provider.BaseColumns;

/**
 * Created by TWI on 02/11/15.
 */
public final class DBContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DBContract() {}

    /* Inner class that defines the table contents */
    public static abstract class Entry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_PATIENT_ID = "identifier";
        public static final String COLUMN_PATIENT_UUID = "uuid";
        public static final String COLUMN_PATIENT_FIRST_NAME = "given_name";
        public static final String COLUMN_PATIENT_MIDDLE_NAME = "middle_name";
        public static final String COLUMN_PATIENT_LAST_NAME = "last_name";
        public static final String COLUMN_PATIENT_JSON = "patient_json";
    }
}
