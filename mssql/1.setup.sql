create database eventuate;
GO
USE eventuate;
GO
create schema eventuate;
GO
DROP table IF EXISTS eventuate.events;
GO
DROP table IF EXISTS  eventuate.entities;
GO
DROP table IF EXISTS  eventuate.snapshots;
GO
DROP table IF EXISTS eventuate.cdc_monitoring;
GO

create table eventuate.events (
  event_id varchar(1000) PRIMARY KEY,
  event_type varchar(1000),
  event_data varchar(1000) NOT NULL,
  entity_type VARCHAR(1000) NOT NULL,
  entity_id VARCHAR(1000) NOT NULL,
  triggering_event VARCHAR(1000),
  metadata VARCHAR(1000),
  published TINYINT DEFAULT 0
);
GO

CREATE INDEX events_idx ON eventuate.events(entity_type, entity_id, event_id);
GO
CREATE INDEX events_published_idx ON eventuate.events(published, event_id);
GO

create table eventuate.entities (
  entity_type VARCHAR(1000),
  entity_id VARCHAR(1000),
  entity_version VARCHAR(1000) NOT NULL,
  PRIMARY KEY(entity_type, entity_id)
);
GO

CREATE INDEX entities_idx ON eventuate.events(entity_type, entity_id);
GO

create table eventuate.snapshots (
  entity_type VARCHAR(1000),
  entity_id VARCHAR(1000),
  entity_version VARCHAR(1000),
  snapshot_type VARCHAR(1000) NOT NULL,
  snapshot_json VARCHAR(1000) NOT NULL,
  triggering_events VARCHAR(1000),
  PRIMARY KEY(entity_type, entity_id, entity_version)
);
GO

create table eventuate.cdc_monitoring (
  reader_id VARCHAR(1000) PRIMARY KEY,
  last_time BIGINT
);
GO