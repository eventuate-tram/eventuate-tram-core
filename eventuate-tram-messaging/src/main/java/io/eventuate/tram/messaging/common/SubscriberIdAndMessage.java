package io.eventuate.tram.messaging.common;

import io.eventuate.tram.messaging.common.Message;

public class SubscriberIdAndMessage {

  private String subscriberId;
  private Message message;

  public SubscriberIdAndMessage(String subscriberId, Message message) {
    this.subscriberId = subscriberId;
    this.message = message;
  }

  public String getSubscriberId() {
    return subscriberId;
  }

  public Message getMessage() {
    return message;
  }
}
