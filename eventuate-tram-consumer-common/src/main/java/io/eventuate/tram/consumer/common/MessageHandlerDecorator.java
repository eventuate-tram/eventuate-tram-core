package io.eventuate.tram.consumer.common;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;

import java.util.function.BiConsumer;

public interface MessageHandlerDecorator extends BiConsumer<SubscriberIdAndMessage, MessageHandlerDecoratorChain> {
  int getOrder();
}
