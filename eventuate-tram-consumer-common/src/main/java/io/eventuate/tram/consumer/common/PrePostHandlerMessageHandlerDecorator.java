package io.eventuate.tram.consumer.common;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import java.util.Arrays;

public class PrePostHandlerMessageHandlerDecorator implements MessageHandlerDecorator, Ordered {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private MessageInterceptor[] messageInterceptors;

  public PrePostHandlerMessageHandlerDecorator(MessageInterceptor[] messageInterceptors) {
    this.messageInterceptors = messageInterceptors;
  }

  @Override
  public void accept(SubscriberIdAndMessage subscriberIdAndMessage, MessageHandlerDecoratorChain messageHandlerDecoratorChain) {
    Message message = subscriberIdAndMessage.getMessage();
    String subscriberId = subscriberIdAndMessage.getSubscriberId();
    preHandle(subscriberId, message);
    try {
      messageHandlerDecoratorChain.invokeNext(subscriberIdAndMessage);
      postHandle(subscriberId, message, null);
    } catch (Throwable t) {
      postHandle(subscriberId, message, t);
      throw t;
    }

  }

  private void preHandle(String subscriberId, Message message) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.preHandle(subscriberId, message));
  }

  private void postHandle(String subscriberId, Message message, Throwable t) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.postHandle(subscriberId, message, t));
  }

  @Override
  public int getOrder() {
    return BuiltInMessageHandlerDecoratorOrder.PRE_POST_HANDLER_MESSAGE_HANDLER_DECORATOR;
  }
}
