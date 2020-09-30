package io.eventuate.tram.messaging.producer.common;

import io.eventuate.tram.messaging.common.Message;

public interface MessageProducerImplementation {

  String send(Message message);

  default void withContext(Runnable runnable) {
    runnable.run();
  }
}
