ALTER TABLE error_log RENAME TO error_log_temp;
CREATE TABLE IF NOT EXISTS error_log (failedRequest TEXT, logDateTime DATETIME, responseStatus INTEGER, stackTrace TEXT, requestPayload TEXT DEFAULT NULL, provider TEXT DEFAULT NULL, PRIMARY KEY(failedRequest, requestPayload));
INSERT INTO error_log (failedRequest, logDateTime, responseStatus, stackTrace) SELECT failedRequest, logDateTime, responseStatus, stackTrace FROM error_log_temp group by failedRequest;
DROP TABLE error_log_temp;