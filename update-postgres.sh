#! /bin/sh

echo "CREATE TABLE eventuate.offset_store(client_name VARCHAR(255) NOT NULL PRIMARY KEY, serialized_offset VARCHAR(255))" | ./postgres-cli.sh -i
echo "ALTER TABLE eventuate.message ADD COLUMN creation_time BIGINT" | ./postgres-cli.sh -i
echo "ALTER TABLE eventuate.received_messages ADD COLUMN creation_time BIGINT" | ./postgres-cli.sh -i