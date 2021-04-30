package io.eventuate.tram.consumer.common;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;

public interface MessageHandlerDecoratorChain {
  void invokeNext(SubscriberIdAndMessage subscriberIdAndMessage);
}
