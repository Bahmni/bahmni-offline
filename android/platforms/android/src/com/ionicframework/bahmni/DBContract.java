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
    public static abstract class PATIENT implements BaseColumns {
        public static final String TABLE_NAME = "patient";
        public static final String COLUMN_PATIENT_ID = "identifier";
        public static final String COLUMN_PATIENT_UUID = "uuid";
        public static final String COLUMN_PATIENT_FIRST_NAME = "givenName";
        public static final String COLUMN_PATIENT_MIDDLE_NAME = "middleName";
        public static final String COLUMN_PATIENT_AGE = "age";
        public static final String COLUMN_PATIENT_GENDER = "gender";
        public static final String COLUMN_PATIENT_LAST_NAME = "familyName";
        public static final String COLUMN_DATE_CREATED = "dateCreated";
        public static final String COLUMN_RURAL_WARD = "address2";
        public static final String COLUMN_PATIENT_JSON = "patient_json";
    }

    /* Inner class that defines the table contents */
    public static abstract class PATIENT_ATTRIBUTES implements BaseColumns {
        public static final String TABLE_NAME = "patient_attributes";
        public static final String COLUMN_PATIENT_ID = "identifier";
    }
}
