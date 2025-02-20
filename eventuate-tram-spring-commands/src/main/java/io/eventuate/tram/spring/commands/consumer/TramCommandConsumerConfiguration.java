package io.eventuate.tram.spring.commands.consumer;

import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.commands.consumer.CommandDispatcherFactory;
import io.eventuate.tram.commands.consumer.CommandReplyProducer;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramCommandReplyProducerConfiguration.class, AnnotationBasedCommandHandlerConfiguration.class})
public class TramCommandConsumerConfiguration {

  @Bean
  public CommandDispatcherFactory commandDispatcherFactory(MessageConsumer messageConsumer, CommandNameMapping commandNameMapping, CommandReplyProducer commandReplyProducer) {
    return new CommandDispatcherFactory(messageConsumer, commandNameMapping, commandReplyProducer);
  }

}
