GRANT ALL PRIVILEGES ON eventuate.* TO 'mysqluser'@'%' WITH GRANT OPTION;
create database custom;
GRANT ALL PRIVILEGES ON custom.* TO 'mysqluser'@'%' WITH GRANT OPTION;

USE custom;

DROP Table IF Exists message;

CREATE TABLE message (
  id VARCHAR(1000) PRIMARY KEY,
  destination VARCHAR(1000) NOT NULL,
  headers VARCHAR(1000) NOT NULL,
  payload VARCHAR(1000) NOT NULL,
  published SMALLINT DEFAULT 0
);

CREATE INDEX message_published_idx ON message(published, id);

CREATE TABLE received_messages (
  consumer_id VARCHAR(1000),
  message_id VARCHAR(1000),
  PRIMARY KEY(consumer_id, message_id)
);

CREATE TABLE aggregate_instance_subscriptions(
  aggregate_type VARCHAR(200) DEFAULT NULL,
  aggregate_id VARCHAR(1000) NOT NULL,
  event_type VARCHAR(200) NOT NULL,
  saga_id VARCHAR(1000) NOT NULL,
  saga_type VARCHAR(200) NOT NULL,
  PRIMARY KEY(aggregate_id, event_type, saga_id, saga_type)
);

CREATE TABLE saga_instance(
  saga_type VARCHAR(100) NOT NULL,
  saga_id VARCHAR(100) NOT NULL,
  state_name VARCHAR(100) NOT NULL,
  last_request_id VARCHAR(100),
  saga_data_type VARCHAR(1000) NOT NULL,
  saga_data_json VARCHAR(1000) NOT NULL,
  PRIMARY KEY(saga_type, saga_id)
);