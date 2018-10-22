CREATE SCHEMA eventuate;

DROP TABLE IF EXISTS eventuate.message CASCADE;
DROP TABLE IF EXISTS eventuate.received_messages CASCADE;
DROP TABLE IF EXISTS eventuate.aggregate_instance_subscriptions CASCADE;
DROP TABLE IF EXISTS eventuate.saga_instance CASCADE;

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

CREATE TABLE eventuate.aggregate_instance_subscriptions(
  aggregate_type VARCHAR(200) DEFAULT NULL,
  aggregate_id VARCHAR(1000) NOT NULL,
  event_type VARCHAR(200) NOT NULL,
  saga_id VARCHAR(1000) NOT NULL,
  saga_type VARCHAR(200) NOT NULL,
  PRIMARY KEY(aggregate_id, event_type, saga_id, saga_type)
);

CREATE TABLE eventuate.saga_instance(
  saga_type VARCHAR(100) NOT NULL,
  saga_id VARCHAR(100) NOT NULL,
  state_name VARCHAR(100) NOT NULL,
  last_request_id VARCHAR(100),
  saga_data_type VARCHAR(1000) NOT NULL,
  saga_data_json VARCHAR(1000) NOT NULL,
  PRIMARY KEY(saga_type, saga_id)
);

CREATE TABLE eventuate.offset_store(
  client_name VARCHAR(255) NOT NULL PRIMARY KEY,
  serialized_offset VARCHAR(255)
);

DROP TABLE IF EXISTS eventuate.events CASCADE;
DROP TABLE IF EXISTS eventuate.entities CASCADE;
DROP TABLE IF EXISTS eventuate.snapshots CASCADE;

CREATE TABLE eventuate.events (
  event_id VARCHAR(1000) PRIMARY KEY,
  event_type VARCHAR(1000),
  event_data VARCHAR(1000) NOT NULL,
  entity_type VARCHAR(1000) NOT NULL,
  entity_id VARCHAR(1000) NOT NULL,
  triggering_event VARCHAR(1000),
  metadata VARCHAR(1000),
  published SMALLINT DEFAULT 0
);

CREATE INDEX events_idx ON eventuate.events(entity_type, entity_id, event_id);
CREATE INDEX events_published_idx ON eventuate.events(published, event_id);

CREATE TABLE eventuate.entities (
  entity_type VARCHAR(1000),
  entity_id VARCHAR(1000),
  entity_version VARCHAR(1000) NOT NULL,
  PRIMARY KEY(entity_type, entity_id)
);

CREATE INDEX entities_idx ON eventuate.events(entity_type, entity_id);

CREATE TABLE eventuate.snapshots (
  entity_type VARCHAR(1000),
  entity_id VARCHAR(1000),
  entity_version VARCHAR(1000),
  snapshot_type VARCHAR(1000) NOT NULL,
  snapshot_json VARCHAR(1000) NOT NULL,
  triggering_events VARCHAR(1000),
  PRIMARY KEY(entity_type, entity_id, entity_version)
);

SELECT * FROM pg_create_logical_replication_slot('eventuate_slot', 'wal2json');
SELECT * FROM pg_create_logical_replication_slot('eventuate_slot2', 'wal2json');