package io.eventuate.tram.spring.commands.consumer;

import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.commands.consumer.CommandDispatcherFactory;
import io.eventuate.tram.commands.consumer.CommandReplyProducer;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TramCommandConsumerConfiguration {

  @Bean
  public CommandDispatcherFactory commandDispatcherFactory(MessageConsumer messageConsumer, CommandNameMapping commandNameMapping, CommandReplyProducer commandReplyProducer) {
    return new CommandDispatcherFactory(messageConsumer, commandNameMapping, commandReplyProducer);
  }

  @Bean
  public CommandReplyProducer commandReplyProducer(MessageProducer messageProducer) {
    return new CommandReplyProducer(messageProducer);
  }
}
