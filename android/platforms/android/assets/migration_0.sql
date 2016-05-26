CREATE TABLE IF NOT EXISTS patient (identifier TEXT, uuid TEXT PRIMARY KEY, givenName TEXT, middleName TEXT, familyName TEXT, gender TEXT, birthdate TEXT, dateCreated TEXT, patientJson TEXT, relationships TEXT);
CREATE TABLE IF NOT EXISTS patient_attribute_types (attributeTypeId INTEGER, attributeName TEXT, uuid TEXT PRIMARY KEY, format TEXT);
CREATE TABLE IF NOT EXISTS patient_attributes (attributeTypeId INTEGER, attributeValue TEXT, patientUuid TEXT, uuid TEXT PRIMARY KEY);
CREATE TABLE IF NOT EXISTS event_log_marker (markerName TEXT PRIMARY KEY, lastReadEventUuid TEXT, catchmentNumber TEXT, lastReadTime DATETIME);
CREATE TABLE IF NOT EXISTS address_hierarchy_entry (id INTEGER, name TEXT, levelId INTEGER, parentId INTEGER, userGeneratedId TEXT, uuid TEXT PRIMARY KEY);
CREATE TABLE IF NOT EXISTS address_hierarchy_level (addressHierarchyLevelId INTEGER, name TEXT, parentLevelId INTEGER, addressField TEXT, uuid TEXT PRIMARY KEY, required INTEGER);
CREATE TABLE IF NOT EXISTS patient_address (address1 TEXT, address2 TEXT, cityVillage TEXT, stateProvince TEXT, postalCode TEXT, country TEXT, countyDistrict TEXT, address3 TEXT, address4 TEXT, address5 TEXT, address6 TEXT, patientUuid TEXT PRIMARY KEY);
CREATE TABLE IF NOT EXISTS idgen (identifier INTEGER PRIMARY KEY);
CREATE TABLE IF NOT EXISTS configs (key TEXT PRIMARY KEY, value TEXT, etag TEXT);
CREATE TABLE IF NOT EXISTS reference_data (key TEXT PRIMARY KEY, data TEXT, etag TEXT);
CREATE TABLE IF NOT EXISTS concept (uuid TEXT PRIMARY KEY, data TEXT, parents TEXT, name TEXT);
CREATE TABLE IF NOT EXISTS login_locations (uuid TEXT PRIMARY KEY, value TEXT);
CREATE TABLE IF NOT EXISTS encounter (uuid TEXT PRIMARY KEY, patientUuid TEXT, encounterType TEXT, providerUuid TEXT, encounterDateTime DATETIME, visitUuid TEXT, encounterJson TEXT);
CREATE TABLE IF NOT EXISTS visit (uuid TEXT PRIMARY KEY, patientUuid TEXT, startDatetime DATETIME, visitJson TEXT);
CREATE TABLE IF NOT EXISTS error_log (id INTEGER PRIMARY KEY AUTOINCREMENT, failedRequest TEXT, logDateTime DATETIME, responseStatus INTEGER, stackTrace TEXT);
CREATE TABLE IF NOT EXISTS observation (uuid TEXT PRIMARY KEY , encounterUuid TEXT, visitUuid TEXT, patientUuid TEXT, conceptName TEXT, observationJson TEXT);



CREATE INDEX IF NOT EXISTS givenNameIndex ON patient(givenName);
CREATE INDEX IF NOT EXISTS middleNameIndex ON patient(middleName);
CREATE INDEX IF NOT EXISTS familyNameIndex ON patient(familyName);
CREATE INDEX IF NOT EXISTS identifierIndex ON patient(identifier);
