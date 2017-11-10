CREATE SCHEMA custom;

DROP TABLE IF EXISTS custom.message CASCADE;
DROP TABLE IF EXISTS custom.received_messages CASCADE;
DROP TABLE IF EXISTS custom.aggregate_instance_subscriptions CASCADE;
DROP TABLE IF EXISTS custom.saga_instance CASCADE;

CREATE TABLE custom.message (
  id VARCHAR(1000) PRIMARY KEY,
  destination VARCHAR(1000) NOT NULL,
  headers VARCHAR(1000) NOT NULL,
  payload VARCHAR(1000) NOT NULL,
  published SMALLINT DEFAULT 0
);

CREATE TABLE custom.received_messages (
  consumer_id VARCHAR(1000),
  message_id VARCHAR(1000),
  PRIMARY KEY(consumer_id, message_id)
);

CREATE TABLE custom.aggregate_instance_subscriptions(
  aggregate_type VARCHAR(200) DEFAULT NULL,
  aggregate_id VARCHAR(1000) NOT NULL,
  event_type VARCHAR(200) NOT NULL,
  saga_id VARCHAR(1000) NOT NULL,
  saga_type VARCHAR(200) NOT NULL,
  PRIMARY KEY(aggregate_id, event_type, saga_id, saga_type)
);

CREATE TABLE custom.saga_instance(
  saga_type VARCHAR(100) NOT NULL,
  saga_id VARCHAR(100) NOT NULL,
  state_name VARCHAR(100) NOT NULL,
  last_request_id VARCHAR(100),
  saga_data_type VARCHAR(1000) NOT NULL,
  saga_data_json VARCHAR(1000) NOT NULL,
  PRIMARY KEY(saga_type, saga_id)
);


DROP TABLE IF EXISTS custom.events CASCADE;
DROP TABLE IF EXISTS custom.entities CASCADE;
DROP TABLE IF EXISTS custom.snapshots CASCADE;

CREATE TABLE custom.events (
  event_id VARCHAR(1000) PRIMARY KEY,
  event_type VARCHAR(1000),
  event_data VARCHAR(1000) NOT NULL,
  entity_type VARCHAR(1000) NOT NULL,
  entity_id VARCHAR(1000) NOT NULL,
  triggering_event VARCHAR(1000),
  metadata VARCHAR(1000),
  published SMALLINT DEFAULT 0
);

CREATE INDEX events_idx ON custom.events(entity_type, entity_id, event_id);
CREATE INDEX events_published_idx ON custom.events(published, event_id);

CREATE TABLE custom.entities (
  entity_type VARCHAR(1000),
  entity_id VARCHAR(1000),
  entity_version VARCHAR(1000) NOT NULL,
  PRIMARY KEY(entity_type, entity_id)
);

CREATE INDEX entities_idx ON custom.events(entity_type, entity_id);

CREATE TABLE custom.snapshots (
  entity_type VARCHAR(1000),
  entity_id VARCHAR(1000),
  entity_version VARCHAR(1000),
  snapshot_type VARCHAR(1000) NOT NULL,
  snapshot_json VARCHAR(1000) NOT NULL,
  triggering_events VARCHAR(1000),
  PRIMARY KEY(entity_type, entity_id, entity_version)
);