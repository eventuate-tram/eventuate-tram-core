package io.eventuate.tram.consumer.common.micronaut;

import io.eventuate.tram.consumer.common.*;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.micronaut.context.annotation.Factory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Factory
public class TramConsumerBaseCommonFactory {

  @Inject
  private MessageInterceptor[] messageInterceptors;

  @Singleton
  public DecoratedMessageHandlerFactory subscribedMessageHandlerChainFactory(List<MessageHandlerDecorator> decorators) {
    return new DecoratedMessageHandlerFactory(decorators);
  }

  @Singleton
  public PrePostReceiveMessageHandlerDecorator prePostReceiveMessageHandlerDecoratorDecorator() {
    return new PrePostReceiveMessageHandlerDecorator(messageInterceptors);
  }

  @Singleton
  public DuplicateDetectingMessageHandlerDecorator duplicateDetectingMessageHandlerDecorator(DuplicateMessageDetector duplicateMessageDetector) {
    return new DuplicateDetectingMessageHandlerDecorator(duplicateMessageDetector);
  }

  @Singleton
  public PrePostHandlerMessageHandlerDecorator prePostHandlerMessageHandlerDecorator() {
    return new PrePostHandlerMessageHandlerDecorator(messageInterceptors);
  }
}
