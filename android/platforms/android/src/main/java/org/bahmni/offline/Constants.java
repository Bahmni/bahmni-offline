package org.bahmni.offline;

public class Constants {
    public static String CREATE_PATIENT_TABLE = "CREATE TABLE IF NOT EXISTS patient (uuid TEXT PRIMARY KEY, givenName TEXT, middleName TEXT, familyName TEXT, gender TEXT, voided BOOLEAN, birthdate TEXT, dateCreated TEXT, patientJson TEXT, relationships TEXT)";
    public static String CREATE_PATIENT_ATTRIBUTE_TYPE_TABLE = "CREATE TABLE IF NOT EXISTS patient_attribute_types (attributeTypeId INTEGER, attributeName TEXT, uuid TEXT PRIMARY KEY, format TEXT);";
    public static String CREATE_PATIENT_ATTRIBUTE_TABLE = "CREATE TABLE IF NOT EXISTS patient_attributes (attributeTypeId INTEGER, attributeValue TEXT, patientUuid TEXT, uuid TEXT PRIMARY KEY);";
    public static String CREATE_ADDRESS_HIERARCHY_ENTRY_TABLE = "CREATE TABLE IF NOT EXISTS address_hierarchy_entry (id INTEGER, name TEXT, levelId INTEGER, parentId INTEGER, userGeneratedId TEXT, uuid TEXT PRIMARY KEY);";
    public static String CREATE_ADDRESS_HIERARCHY_LEVEL_TABLE = "CREATE TABLE IF NOT EXISTS address_hierarchy_level (addressHierarchyLevelId INTEGER, name TEXT, parentLevelId INTEGER, addressField TEXT, uuid TEXT PRIMARY KEY, required INTEGER);";
    public static String CREATE_PATIENT_ADDRESS_TABLE = "CREATE TABLE IF NOT EXISTS patient_address (address1 TEXT, address2 TEXT, cityVillage TEXT, stateProvince TEXT, postalCode TEXT, country TEXT, countyDistrict TEXT, address3 TEXT, address4 TEXT, address5 TEXT, address6 TEXT, patientUuid TEXT PRIMARY KEY);";
    public static String CREATE_CONFIG_TABLE= "CREATE TABLE IF NOT EXISTS configs (key TEXT PRIMARY KEY, value TEXT, etag TEXT);";
    public static String CREATE_REFERENCE_DATA_TABLE= "CREATE TABLE IF NOT EXISTS reference_data (key TEXT PRIMARY KEY, data TEXT, etag TEXT);";
    public static String CREATE_CONCEPT_TABLE= "CREATE TABLE IF NOT EXISTS concept (uuid TEXT PRIMARY KEY, data TEXT, parents TEXT, name TEXT);";
    public static String CREATE_LOGIN_LOCATIONS_TABLE= "CREATE TABLE IF NOT EXISTS login_locations (uuid TEXT PRIMARY KEY, value TEXT);";
    public static String CREATE_ENCOUNTER_TABLE = "CREATE TABLE IF NOT EXISTS encounter (uuid TEXT PRIMARY KEY, patientUuid TEXT, encounterType TEXT, providerUuid TEXT, encounterDateTime DATETIME, visitUuid TEXT, encounterJson TEXT);";
    public static String CREATE_ERRORLOG_TABLE = "CREATE TABLE IF NOT EXISTS error_log (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT, failedRequest TEXT, logDateTime DATETIME, responseStatus INTEGER, stackTrace TEXT, requestPayload TEXT, provider TEXT);";
    public static String CREATE_VISIT_TABLE = "CREATE TABLE IF NOT EXISTS visit (uuid TEXT PRIMARY KEY, patientUuid TEXT, startDatetime DATETIME, visitJson TEXT);";
    public static String CREATE_OBSERVATION_TABLE = "CREATE TABLE IF NOT EXISTS observation(uuid TEXT PRIMARY KEY , encounterUuid TEXT, visitUuid TEXT, patientUuid TEXT, conceptName TEXT, observationJson TEXT);";
    public static String CREATE_PATIENT_IDENTIFIER_TABLE = "CREATE TABLE IF NOT EXISTS patient_identifier(identifier TEXT , patientUuid TEXT, typeUuid TEXT, identifierJson TEXT, PRIMARY KEY(patientUuid, typeUuid));";
    public static String APP_UPGRADE_SHARED_PREFERENCE= "AppUpgradeInfo";
    public static String DOWNLOAD_REFERNCE= "downloadReference";
    public static String INSTALL_PENDING= "installPending";
    public static String LAST_DOWNLOADED_APK_NAME= "lastDownloadedApkName";
}
