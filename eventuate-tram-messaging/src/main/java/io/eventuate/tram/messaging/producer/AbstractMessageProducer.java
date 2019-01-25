package io.eventuate.tram.messaging.producer;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageInterceptor;

import java.util.Arrays;

public abstract class AbstractMessageProducer {

  protected MessageInterceptor[] messageInterceptors;

  protected AbstractMessageProducer(MessageInterceptor[] messageInterceptors) {
    this.messageInterceptors = messageInterceptors;
  }

  protected void preSend(Message message) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.preSend(message));
  }


  protected void postSend(Message message, RuntimeException e) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.postSend(message, e));
  }

  protected void sendMessage(String id, String destination, Message message, MessageSender messageSender) {
    if (id == null) {
      if (!message.getHeader(Message.ID).isPresent())
        throw new IllegalArgumentException("message needs an id");
    } else {
      message.getHeaders().put(Message.ID, id);
    }

    message.getHeaders().put(Message.DESTINATION, destination);

    message.getHeaders().put(Message.DATE, HttpDateHeaderFormatUtil.nowAsHttpDateString());

    preSend(message);
    try {
      messageSender.send(message);
      postSend(message, null);
    } catch (RuntimeException e) {
      postSend(message, e);
      throw e;
    }
  }

}
