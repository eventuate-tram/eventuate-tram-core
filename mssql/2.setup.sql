USE eventuate;
GO

DROP Table IF Exists eventuate.message;
GO
DROP Table IF Exists eventuate.received_messages;
GO

CREATE TABLE eventuate.message (
  id VARCHAR(767) PRIMARY KEY,
  destination VARCHAR(1000) NOT NULL,
  headers VARCHAR(1000) NOT NULL,
  payload VARCHAR(1000) NOT NULL,
  published SMALLINT DEFAULT 0,
  creation_time BIGINT
);
GO

CREATE INDEX message_published_idx ON eventuate.message(published, id);
GO

CREATE TABLE eventuate.received_messages (
  consumer_id VARCHAR(767),
  message_id VARCHAR(767),
  PRIMARY KEY(consumer_id, message_id),
  creation_time BIGINT
);
GO

CREATE TABLE eventuate.offset_store(
  client_name VARCHAR(255) NOT NULL PRIMARY KEY,
  serialized_offset VARCHAR(255)
);
GO
