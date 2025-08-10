package io.eventuate.tram.micronaut.consumer.common;

import io.eventuate.tram.consumer.common.*;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.micronaut.context.annotation.Factory;

import jakarta.inject.Singleton;
import java.util.List;

@Factory
public class TramConsumerBaseCommonFactory {

  @Singleton
  public DecoratedMessageHandlerFactory subscribedMessageHandlerChainFactory(List<MessageHandlerDecorator> decorators) {
    return new DecoratedMessageHandlerFactory(decorators);
  }

  @Singleton
  public PrePostReceiveMessageHandlerDecorator prePostReceiveMessageHandlerDecoratorDecorator(MessageInterceptor[] messageInterceptors) {
    return new PrePostReceiveMessageHandlerDecorator(messageInterceptors);
  }

  @Singleton
  public DuplicateDetectingMessageHandlerDecorator duplicateDetectingMessageHandlerDecorator(DuplicateMessageDetector duplicateMessageDetector) {
    return new DuplicateDetectingMessageHandlerDecorator(duplicateMessageDetector);
  }

  @Singleton
  public PrePostHandlerMessageHandlerDecorator prePostHandlerMessageHandlerDecorator(MessageInterceptor[] messageInterceptors) {
    return new PrePostHandlerMessageHandlerDecorator(messageInterceptors);
  }
}
