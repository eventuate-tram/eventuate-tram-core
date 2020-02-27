package io.eventuate.tram.spring.consumer.common;

import io.eventuate.tram.consumer.common.DecoratedMessageHandlerFactory;
import io.eventuate.tram.consumer.common.MessageConsumerImpl;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TramConsumerBaseCommonConfiguration.class)
public class TramConsumerCommonConfiguration {

  @Bean
  public MessageConsumer messageConsumer(MessageConsumerImplementation messageConsumerImplementation,
                                         ChannelMapping channelMapping,
                                         DecoratedMessageHandlerFactory decoratedMessageHandlerFactory) {
    return new MessageConsumerImpl(channelMapping, messageConsumerImplementation, decoratedMessageHandlerFactory);
  }
}
