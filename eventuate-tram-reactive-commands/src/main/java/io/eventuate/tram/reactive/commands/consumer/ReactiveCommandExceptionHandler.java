package io.eventuate.tram.reactive.commands.consumer;


import io.eventuate.tram.messaging.common.Message;
import org.reactivestreams.Publisher;

public class ReactiveCommandExceptionHandler {
  public Publisher<Message> invoke(Throwable cause) {
    throw new UnsupportedOperationException();
  }
}
