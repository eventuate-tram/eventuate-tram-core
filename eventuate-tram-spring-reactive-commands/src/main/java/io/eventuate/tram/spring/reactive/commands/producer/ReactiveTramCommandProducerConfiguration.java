package io.eventuate.tram.spring.reactive.commands.producer;

import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.reactive.commands.producer.ReactiveCommandProducer;
import io.eventuate.tram.reactive.commands.producer.ReactiveCommandProducerImpl;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactiveTramCommandProducerConfiguration {

  @Bean
  public ReactiveCommandProducer commandProducer(ReactiveMessageProducer messageProducer, CommandNameMapping commandNameMapping) {
    return new ReactiveCommandProducerImpl(messageProducer, commandNameMapping);
  }

}
