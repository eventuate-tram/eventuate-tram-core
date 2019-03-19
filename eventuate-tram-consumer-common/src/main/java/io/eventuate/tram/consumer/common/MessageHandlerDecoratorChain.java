package io.eventuate.tram.consumer.common;

public interface MessageHandlerDecoratorChain {
  void invokeNext(SubscriberIdAndMessage subscriberIdAndMessage);
}
