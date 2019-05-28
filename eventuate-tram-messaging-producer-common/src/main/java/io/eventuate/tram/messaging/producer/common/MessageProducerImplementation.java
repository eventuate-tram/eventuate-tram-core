package io.eventuate.tram.messaging.producer.common;

import io.eventuate.tram.messaging.common.Message;

public interface MessageProducerImplementation {

  void send(Message message);
  String generateMessageId();

  default void withContext(Runnable runnable) {
    runnable.run();
  }
}
