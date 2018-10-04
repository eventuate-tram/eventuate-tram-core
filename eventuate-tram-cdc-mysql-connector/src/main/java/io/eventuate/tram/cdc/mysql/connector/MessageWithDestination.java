package io.eventuate.tram.cdc.mysql.connector;

import io.eventuate.local.common.BinLogEvent;
import io.eventuate.local.common.BinlogFileOffset;
import io.eventuate.tram.messaging.common.MessageImpl;

import java.util.Optional;

public class MessageWithDestination implements BinLogEvent {
  private final String destination;
  private final MessageImpl message;
  private BinlogFileOffset binlogFileOffset;

  public MessageWithDestination(String destination, MessageImpl message, BinlogFileOffset binlogFileOffset) {
    this.destination = destination;
    this.message = message;
    this.binlogFileOffset = binlogFileOffset;
  }

  public String getDestination() {
    return destination;
  }

  public MessageImpl getMessage() {
    return message;
  }

  public Optional<BinlogFileOffset> getBinlogFileOffset() {
    return Optional.ofNullable(binlogFileOffset);
  }
}
