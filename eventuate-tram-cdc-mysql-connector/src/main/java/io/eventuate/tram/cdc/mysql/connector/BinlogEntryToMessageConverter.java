package io.eventuate.tram.cdc.mysql.connector;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.local.common.BinlogEntry;
import io.eventuate.local.common.BinlogEntryToEventConverter;
import io.eventuate.tram.messaging.common.MessageImpl;

import java.util.Map;

public class BinlogEntryToMessageConverter implements BinlogEntryToEventConverter<MessageWithDestination> {
  @Override
  public MessageWithDestination convert(BinlogEntry binlogEntry) {
    return new MessageWithDestination((String)binlogEntry.getColumn("destination"),
            new MessageImpl((String)binlogEntry.getColumn("payload"),
                    JSonMapper.fromJson((String)binlogEntry.getColumn("headers"), Map.class)),
            binlogEntry.getBinlogFileOffset());
  }
}
