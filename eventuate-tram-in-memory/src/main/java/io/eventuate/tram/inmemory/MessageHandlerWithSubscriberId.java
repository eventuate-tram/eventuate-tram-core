package io.eventuate.tram.inmemory;

import io.eventuate.tram.consumer.common.SubscriberIdAndMessage;

import java.util.function.Consumer;

public class MessageHandlerWithSubscriberId {

  private String subscriber;
  private Consumer<SubscriberIdAndMessage> messageHandler;

  public MessageHandlerWithSubscriberId(String subscriber, Consumer<SubscriberIdAndMessage> messageHandler) {
    this.subscriber = subscriber;
    this.messageHandler = messageHandler;
  }

  public String getSubscriber() {
    return subscriber;
  }

  public Consumer<SubscriberIdAndMessage> getMessageHandler() {
    return messageHandler;
  }
}
