CREATE DATABASE IF NOT EXISTS custom;
GRANT ALL PRIVILEGES ON custom.* TO 'mysqluser'@'%' WITH GRANT OPTION;

USE custom;

DROP Table IF Exists message;
DROP Table IF Exists received_messages;
DROP Table IF Exists offset_store;

CREATE TABLE message (
  id VARCHAR(255) PRIMARY KEY,
  destination VARCHAR(1000) NOT NULL,
  headers VARCHAR(1000) NOT NULL,
  payload VARCHAR(1000) NOT NULL,
  published SMALLINT DEFAULT 0,
  message_partition SMALLINT,
  creation_time BIGINT
);

CREATE INDEX message_published_idx ON message(published, id);

CREATE TABLE received_messages (
  consumer_id VARCHAR(255),
  message_id VARCHAR(255),
  PRIMARY KEY(consumer_id, message_id),
  creation_time BIGINT
);

CREATE TABLE offset_store(
  client_name VARCHAR(255) NOT NULL PRIMARY KEY,
  serialized_offset VARCHAR(255)
);