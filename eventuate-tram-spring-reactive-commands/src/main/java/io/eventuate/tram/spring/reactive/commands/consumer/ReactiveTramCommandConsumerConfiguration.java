package io.eventuate.tram.spring.reactive.commands.consumer;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandDispatcherFactory;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactiveTramCommandConsumerConfiguration {

  @Bean
  public ReactiveCommandDispatcherFactory commandDispatcherFactory(ReactiveMessageConsumer messageConsumer, ReactiveMessageProducer messageProducer) {
    return new ReactiveCommandDispatcherFactory(messageConsumer, messageProducer);
  }
}
