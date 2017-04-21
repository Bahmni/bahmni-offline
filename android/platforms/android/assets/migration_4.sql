/*  Move common data to metadata database for supporting multiple database */
END TRANSACTION;
ATTACH @metaDataDbPath as metadata KEY @encryptionKey;
BEGIN TRANSACTION;

insert or replace into metadata.event_log_marker select * from event_log_marker where markerName="offline-concepts";
insert or replace into metadata.login_locations select * from login_locations;
insert or replace into metadata.concept select * from concept;
insert or replace into metadata.reference_data select * from reference_data;
insert or replace into metadata.configs select * from configs;

END TRANSACTION;
DETACH DATABASE 'metadata';
BEGIN TRANSACTION;

delete from event_log_marker where markerName="offline-concepts";
DROP TABLE login_locations;
DROP TABLE concept;
DROP TABLE reference_data;
DROP TABLE configs;

/* Split transactionalData into patient data and encounter data */
INSERT INTO event_log_marker(markerName, lastReadEventUuid , filters , lastReadTime ) select * from ( select "patient", lastReadEventUuid , filters , lastReadTime  FROM event_log_marker where markerName="transactionalData");
INSERT INTO event_log_marker(markerName, lastReadEventUuid , filters , lastReadTime ) select * from ( select "encounter", lastReadEventUuid , filters , lastReadTime  FROM event_log_marker where markerName="transactionalData");
delete from event_log_marker where markerName="transactionalData";