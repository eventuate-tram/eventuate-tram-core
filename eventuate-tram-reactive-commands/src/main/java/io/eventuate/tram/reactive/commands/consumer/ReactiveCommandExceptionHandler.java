package io.eventuate.tram.reactive.commands.consumer;


import io.eventuate.tram.messaging.common.Message;
import org.reactivestreams.Publisher;

import java.util.List;

public class ReactiveCommandExceptionHandler {
  public Publisher<List<Message>> invoke(Throwable cause) {
    throw new UnsupportedOperationException();
  }
}
