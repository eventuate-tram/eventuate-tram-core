package io.eventuate.tram.consumer.common;

import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TramConsumerBaseCommonConfiguration.class)
public class TramConsumerCommonConfiguration {

  @Bean
  public MessageConsumer messageConsumer(MessageConsumerImplementation messageConsumerImplementation, ChannelMapping channelMapping) {
    return new ChannelMappingMessageConsumer(channelMapping, messageConsumerImplementation);
  }
}
