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
    public static String[] EVENT_LOG_MARKER_COLUMN_NAMES = {
            "lastReadEventUuid",
            "catchmentNumber",
            "lastReadTime"
    };
    public static String[] ADDRESS_HIERARCHY_ENTRY_COLUMN_NAMES = {
            "name",
            "level_id",
            "parent_id",
            "user_generated_id",
            "uuid"
    };
    public static String[] ADDRESS_HIERARCHY_LEVEL_COLUMN_NAMES = {
            "address_hierarchy_level_id",
            "name",
            "parent_level_id",
            "address_field",
            "uuid",
            "required"
    };
    public static String KEY = "key";

}
