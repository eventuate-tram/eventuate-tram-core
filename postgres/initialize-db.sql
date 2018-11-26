DROP TABLE IF EXISTS eventuate.message CASCADE;
DROP TABLE IF EXISTS eventuate.received_messages CASCADE;

CREATE TABLE eventuate.message (
  id VARCHAR(1000) PRIMARY KEY,
  destination VARCHAR(1000) NOT NULL,
  headers VARCHAR(1000) NOT NULL,
  payload VARCHAR(1000) NOT NULL,
  published SMALLINT DEFAULT 0,
  creation_time BIGINT
);

CREATE TABLE eventuate.received_messages (
  consumer_id VARCHAR(1000),
  message_id VARCHAR(1000),
  creation_time BIGINT,
  PRIMARY KEY(consumer_id, message_id)
);

CREATE TABLE eventuate.offset_store(
  client_name VARCHAR(255) NOT NULL PRIMARY KEY,
  serialized_offset VARCHAR(255)
);

SELECT * FROM pg_create_logical_replication_slot('eventuate_slot2', 'wal2json');