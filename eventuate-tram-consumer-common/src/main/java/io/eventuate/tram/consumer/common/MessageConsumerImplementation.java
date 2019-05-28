package io.eventuate.tram.consumer.common;

import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;

import java.util.Set;

public interface MessageConsumerImplementation {
  MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler);
  String getId();
  void close();
}
