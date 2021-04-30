package io.eventuate.tram.spring.reactive.consumer.common;

import io.eventuate.tram.consumer.common.reactive.DecoratedReactiveMessageHandlerFactory;
import io.eventuate.tram.consumer.common.reactive.ReactiveDuplicateDetectingMessageHandlerDecorator;
import io.eventuate.tram.consumer.common.reactive.ReactiveDuplicateMessageDetector;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageHandlerDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ReactiveTramConsumerBaseCommonConfiguration {

  @Bean
  public DecoratedReactiveMessageHandlerFactory subscribedMessageHandlerChainFactory(List<ReactiveMessageHandlerDecorator> decorators) {
    return new DecoratedReactiveMessageHandlerFactory(decorators);
  }

  @Bean
  public ReactiveDuplicateDetectingMessageHandlerDecorator duplicateDetectingMessageHandlerDecorator(ReactiveDuplicateMessageDetector duplicateMessageDetector) {
    return new ReactiveDuplicateDetectingMessageHandlerDecorator(duplicateMessageDetector);
  }
}
