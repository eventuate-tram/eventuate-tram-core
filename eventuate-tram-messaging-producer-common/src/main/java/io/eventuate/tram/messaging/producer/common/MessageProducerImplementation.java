package io.eventuate.tram.messaging.producer.common;

import io.eventuate.tram.messaging.common.Message;

public interface MessageProducerImplementation {

  void send(Message message);

  default void setMessageIdIfNecessary(Message message) {
    //do nothing by default
  }

  default void withContext(Runnable runnable) {
    runnable.run();
  }
}
