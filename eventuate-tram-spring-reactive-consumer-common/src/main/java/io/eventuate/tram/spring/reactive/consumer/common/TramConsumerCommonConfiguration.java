package io.eventuate.tram.spring.reactive.consumer.common;

import io.eventuate.tram.consumer.common.reactive.DecoratedReactiveMessageHandlerFactory;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumerImpl;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumerImplementation;
import io.eventuate.tram.messaging.common.ChannelMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ReactiveTramConsumerBaseCommonConfiguration.class)
public class TramConsumerCommonConfiguration {

  @Bean
  public ReactiveMessageConsumer messageConsumer(ReactiveMessageConsumerImplementation messageConsumerImplementation,
                                                 ChannelMapping channelMapping,
                                                 DecoratedReactiveMessageHandlerFactory decoratedMessageHandlerFactory) {
    return new ReactiveMessageConsumerImpl(channelMapping, messageConsumerImplementation, decoratedMessageHandlerFactory);
  }
}
