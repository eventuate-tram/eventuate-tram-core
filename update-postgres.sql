CREATE TABLE offset_store(client_name VARCHAR(255) NOT NULL PRIMARY KEY, serialized_offset VARCHAR(255));
ALTER TABLE message ADD COLUMN creation_time BIGINT;
ALTER TABLE received_messages ADD COLUMN creation_time BIGINT;