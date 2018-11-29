package io.eventuate.tram.inmemory;

import io.eventuate.tram.messaging.consumer.MessageHandler;

public class MessageHandlerWithSubscriberId {

  private String subscriber;
  private MessageHandler messageHandler;

  public MessageHandlerWithSubscriberId(String subscriber, MessageHandler messageHandler) {
    this.subscriber = subscriber;
    this.messageHandler = messageHandler;
  }

  public String getSubscriber() {
    return subscriber;
  }

  public MessageHandler getMessageHandler() {
    return messageHandler;
  }
}
