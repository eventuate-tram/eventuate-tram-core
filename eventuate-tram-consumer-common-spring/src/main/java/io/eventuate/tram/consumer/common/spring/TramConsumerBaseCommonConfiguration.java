package io.eventuate.tram.consumer.common.spring;

import io.eventuate.tram.consumer.common.*;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class TramConsumerBaseCommonConfiguration {

  @Autowired(required=false)
  private MessageInterceptor[] messageInterceptors = new MessageInterceptor[0];

  @Bean
  public DecoratedMessageHandlerFactory subscribedMessageHandlerChainFactory(List<MessageHandlerDecorator> decorators) {
    return new DecoratedMessageHandlerFactory(decorators);
  }

  @Bean
  public PrePostReceiveMessageHandlerDecorator prePostReceiveMessageHandlerDecoratorDecorator() {
    return new PrePostReceiveMessageHandlerDecorator(messageInterceptors);
  }

  @Bean
  public DuplicateDetectingMessageHandlerDecorator duplicateDetectingMessageHandlerDecorator(DuplicateMessageDetector duplicateMessageDetector) {
    return new DuplicateDetectingMessageHandlerDecorator(duplicateMessageDetector);
  }

  @Bean
  public PrePostHandlerMessageHandlerDecorator prePostHandlerMessageHandlerDecorator() {
    return new PrePostHandlerMessageHandlerDecorator(messageInterceptors);
  }
}
