package io.eventuate.tram.spring.commands.producer;

import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.commands.producer.CommandProducerImpl;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TramCommandProducerConfiguration {

  @Bean
  @ConditionalOnMissingBean(CommandProducer.class)
  public CommandProducer commandProducer(MessageProducer messageProducer, ChannelMapping channelMapping, CommandNameMapping commandNameMapping) {
    return new CommandProducerImpl(messageProducer, commandNameMapping);
  }

}
