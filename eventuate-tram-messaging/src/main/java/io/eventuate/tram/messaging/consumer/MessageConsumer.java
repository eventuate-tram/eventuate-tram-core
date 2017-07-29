package io.eventuate.tram.messaging.consumer;

import java.util.Set;

public interface MessageConsumer {

  void subscribe(String subscriberId, Set<String> channels, MessageHandler handler);
}
