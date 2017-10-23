package io.eventuate.tram.commands.consumer;

import io.eventuate.tram.messaging.common.Message;

import java.util.List;

public class CommandExceptionHandler {
  public List<Message> invoke(Throwable cause) {
    throw new UnsupportedOperationException();
  }
}
