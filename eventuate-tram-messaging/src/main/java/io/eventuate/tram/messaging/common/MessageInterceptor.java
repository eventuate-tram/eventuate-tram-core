package io.eventuate.tram.messaging.common;

public interface MessageInterceptor {

  default void preSend(Message message) {}
  default void postSend(Message message, Exception e) {}

  default void preReceive(Message message) {}
  default void preHandle(String subscriberId, Message message) {}
  default void postHandle(String subscriberId, Message message, Throwable throwable) {}
  default void postReceive(Message message) {}

}
