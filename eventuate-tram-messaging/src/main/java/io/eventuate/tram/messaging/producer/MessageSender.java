package io.eventuate.tram.messaging.producer;

import io.eventuate.tram.messaging.common.Message;

public interface MessageSender {
  void send(Message message);
}
