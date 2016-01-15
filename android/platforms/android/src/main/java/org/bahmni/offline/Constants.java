package org.bahmni.offline;

public class Constants {
    public static String[] PATIENT_COLUMN_NAMES = {
            "identifier",
            "uuid",
            "givenName",
            "middleName",
            "familyName",
            "gender",
            "birthdate",
            "dateCreated",
            "patientJson",
            "relationships"};
    public static String[] ATTRIBUTE_TYPE_COLUMN_NAMES = {
            "attributeTypeId",
            "uuid",
            "attributeName",
            "format"
    };
    public static String[] ATTRIBUTE_COLUMN_NAMES = {
            "attributeTypeId",
            "attributeValue",
            "patientId"
    };
    public static String KEY = "key";

}
