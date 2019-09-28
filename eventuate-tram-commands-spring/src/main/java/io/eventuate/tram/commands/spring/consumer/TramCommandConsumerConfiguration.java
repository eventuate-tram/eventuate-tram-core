package io.eventuate.tram.commands.spring.consumer;

import io.eventuate.tram.commands.consumer.CommandDispatcherFactory;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TramCommandConsumerConfiguration {

  @Bean
  public CommandDispatcherFactory commandDispatcherFactory(MessageConsumer messageConsumer, MessageProducer messageProducer) {
    return new CommandDispatcherFactory(messageConsumer, messageProducer);
  }
}
