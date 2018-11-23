#! /bin/sh

echo "CREATE TABLE eventuate.offset_store(client_name VARCHAR(255) NOT NULL PRIMARY KEY, serialized_offset VARCHAR(255))" | ./mysql-cli.sh -i
echo "ALTER TABLE eventuate.message ADD creation_time BIGINT" | ./mysql-cli.sh -i
echo "ALTER TABLE eventuate.received_messages ADD creation_time BIGINT" | ./mysql-cli.sh -i
