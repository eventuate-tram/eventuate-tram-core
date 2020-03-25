package io.eventuate.tram.spring.commands.producer;

import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.commands.producer.CommandProducerImpl;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TramCommandProducerConfiguration {

  @Bean
  public CommandProducer commandProducer(MessageProducer messageProducer, ChannelMapping channelMapping) {
    return new CommandProducerImpl(messageProducer);
  }

}
