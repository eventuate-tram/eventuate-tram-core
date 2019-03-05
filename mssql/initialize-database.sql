IF (NOT EXISTS(
    SELECT name
    FROM sys.schemas
    WHERE name = '$(TRAM_SCHEMA)'))
BEGIN
    EXEC('CREATE SCHEMA [$(TRAM_SCHEMA)]')
END

DROP TABLE IF EXISTS $(TRAM_SCHEMA).message;
DROP TABLE IF EXISTS $(TRAM_SCHEMA).received_messages;

CREATE TABLE $(TRAM_SCHEMA).message (
  id NVARCHAR(200) PRIMARY KEY,
  destination NVARCHAR(1000) NOT NULL,
  headers NVARCHAR(1000) NOT NULL,
  payload NVARCHAR(MAX) NOT NULL,
  published SMALLINT DEFAULT 0
);

CREATE TABLE $(TRAM_SCHEMA).received_messages (
  consumer_id NVARCHAR(200),
  message_id NVARCHAR(200),
  PRIMARY KEY(consumer_id, message_id)
);
