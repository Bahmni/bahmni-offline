package org.bahmni.offline;

public class Constants {
    public static String CREATE_PATIENT_TABLE = "CREATE TABLE patient (identifier TEXT, uuid TEXT PRIMARY KEY, givenName TEXT, middleName TEXT, familyName TEXT, gender TEXT, birthdate TEXT, dateCreated TEXT, patientJson TEXT, relationships TEXT)";
    public static String CREATE_PATIENT_ATTRIBUTE_TYPE_TABLE = "CREATE TABLE patient_attribute_types (attributeTypeId INTEGER, attributeName TEXT, uuid TEXT PRIMARY KEY, format TEXT);";
    public static String CREATE_PATIENT_ATTRIBUTE_TABLE = "CREATE TABLE patient_attributes (attributeTypeId INTEGER, attributeValue TEXT, patientUuid TEXT, uuid TEXT PRIMARY KEY);";
    public static String CREATE_EVENT_LOG_MARKER_TABLE = "CREATE TABLE event_log_marker (lastReadEventUuid TEXT, catchmentNumber TEXT PRIMARY KEY, lastReadTime DATETIME);";
    public static String CREATE_ADDRESS_HIERARCHY_ENTRY_TABLE = "CREATE TABLE address_hierarchy_entry (name TEXT, level_id INTEGER, parent_id INTEGER, user_generated_id TEXT, uuid TEXT PRIMARY KEY);";
    public static String CREATE_ADDRESS_HIERARCHY_LEVEL_TABLE = "CREATE TABLE address_hierarchy_entry (address_hierarchy_level_id INTEGER, name TEXT, parent_level_id INTEGER, address_field TEXT, uuid TEXT PRIMARY KEY, required INTEGER);";
    public static String CREATE_IDGEN_TABLE = "CREATE TABLE idgen (identifier INTEGER PRIMARY KEY);";

    public static String KEY = "key";
}
