package io.eventuate.tram.messaging.producer;

import io.eventuate.tram.messaging.common.Message;

public interface MessageProducer {

  void send(String destination, Message message);

}
