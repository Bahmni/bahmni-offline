CREATE TABLE IF NOT EXISTS configs (key TEXT PRIMARY KEY, value TEXT, etag TEXT);
CREATE TABLE IF NOT EXISTS reference_data (key TEXT PRIMARY KEY, data TEXT, etag TEXT);
CREATE TABLE IF NOT EXISTS concept (uuid TEXT PRIMARY KEY, data TEXT, parents TEXT, name TEXT);
CREATE TABLE IF NOT EXISTS login_locations (uuid TEXT PRIMARY KEY, value TEXT);
CREATE TABLE IF NOT EXISTS event_log_marker (markerName TEXT PRIMARY KEY, lastReadEventUuid TEXT, filters TEXT, lastReadTime DATETIME);
