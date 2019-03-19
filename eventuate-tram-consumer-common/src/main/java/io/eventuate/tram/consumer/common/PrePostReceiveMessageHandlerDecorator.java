package io.eventuate.tram.consumer.common;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import java.util.Arrays;

public class PrePostReceiveMessageHandlerDecorator implements MessageHandlerDecorator, Ordered {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private MessageInterceptor[] messageInterceptors;

  public PrePostReceiveMessageHandlerDecorator(MessageInterceptor[] messageInterceptors) {
    this.messageInterceptors = messageInterceptors;
  }

  @Override
  public void accept(SubscriberIdAndMessage subscriberIdAndMessage, MessageHandlerDecoratorChain messageHandlerDecoratorChain) {
    Message message = subscriberIdAndMessage.getMessage();
    preReceive(message);
    try {
      messageHandlerDecoratorChain.invokeNext(subscriberIdAndMessage);
    } finally {
      postReceive(message);
    }
  }

  private void preReceive(Message message) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.preReceive(message));
  }


  private void postReceive(Message message) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.postReceive(message));
  }

  @Override
  public int getOrder() {
    return BuiltInMessageHandlerDecoratorOrder.PRE_POST_RECEIVE_MESSAGE_HANDLER_DECORATOR;
  }
}
