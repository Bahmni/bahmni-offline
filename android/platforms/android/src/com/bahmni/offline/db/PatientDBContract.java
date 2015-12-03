package com.bahmni.offline.db;

import android.provider.BaseColumns;

public final class PatientDBContract {
    public PatientDBContract() {}

    public static abstract class PATIENT implements BaseColumns {
        public static final String TABLE_NAME = "patient";
        public static final String COLUMN_PATIENT_IDENTIFIER = "identifier";
        public static final String COLUMN_PATIENT_UUID = "uuid";
        public static final String COLUMN_PATIENT_FIRST_NAME = "givenName";
        public static final String COLUMN_PATIENT_MIDDLE_NAME = "middleName";
        public static final String COLUMN_PATIENT_AGE = "age";
        public static final String COLUMN_PATIENT_GENDER = "gender";
        public static final String COLUMN_PATIENT_LAST_NAME = "familyName";
        public static final String COLUMN_DATE_CREATED = "dateCreated";
        public static final String COLUMN_PATIENT_JSON = "patientJson";
    }

    public static abstract class PATIENT_ATTRIBUTES_TYPES implements BaseColumns {
        public static final String TABLE_NAME = "patient_attribute_types";
        public static final String COLUMN_ATTRIBUTE_TYPE_ID = "attributeTypeId";
        public static final String COLUMN_ATTRIBUTE_NAME = "attributeName";
    }
    public static abstract class PATIENT_ATTRIBUTES implements BaseColumns {
        public static final String TABLE_NAME = "patient_attributes";
        public static final String COLUMN_ATTRIBUTE_TYPE_ID = "attributeTypeId";
        public static final String COLUMN_ATTRIBUTE_VALUE = "attributeValue";
        public static final String COLUMN_PATIENT_ID = "patientId";
    }

    public static abstract class PATIENT_ADDRESS implements BaseColumns {
        public static final String TABLE_NAME = "patient_address";
        public static final String COLUMN_PATIENT_ID = "patientId";
    }

    }
