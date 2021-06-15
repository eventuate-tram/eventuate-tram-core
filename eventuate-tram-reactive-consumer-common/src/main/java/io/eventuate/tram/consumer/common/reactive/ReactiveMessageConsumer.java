package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.consumer.MessageSubscription;

import java.util.Set;

public interface ReactiveMessageConsumer {
  MessageSubscription subscribe(String subscriberId, Set<String> channels, ReactiveMessageHandler handler);
  String getId();
  void close();
}
