package io.eventuate.tram.cdc.mysql.connector;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.local.postgres.wal.PostgresWalChange;
import io.eventuate.local.postgres.wal.PostgresWalMessage;
import io.eventuate.local.postgres.wal.PostgresWalMessageParser;
import io.eventuate.tram.messaging.common.MessageImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PostgresWalJsonMessageParser implements PostgresWalMessageParser<MessageWithDestination> {

  @Override
  public List<MessageWithDestination> parse(PostgresWalMessage message, long lastSequenceNumber, String slotName) {
    List<PostgresWalChange> changes = Arrays.asList(message.getChange());

    return changes
            .stream()
            .filter(change -> change.getKind().equals("insert") && change.getTable().equals("message"))
            .map(insertedEvent -> {
              List<String> columns = Arrays.asList(insertedEvent.getColumnnames());

              List<String> values = Arrays.asList(insertedEvent.getColumnvalues());

              int destination = columns.indexOf("destination");
              int payload = columns.indexOf("payload");
              int headers = columns.indexOf("headers");

              return new MessageWithDestination(values.get(destination),
                      new MessageImpl(values.get(payload), JSonMapper.fromJson(values.get(headers), Map.class)),
                      new BinlogFileOffset(slotName, lastSequenceNumber));
            })
            .collect(Collectors.toList());
  }
}
