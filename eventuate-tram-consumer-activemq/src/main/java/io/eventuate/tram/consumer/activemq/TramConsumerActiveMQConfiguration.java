package io.eventuate.tram.consumer.activemq;

import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TramConsumerCommonConfiguration.class)
public class TramConsumerActiveMQConfiguration {
  @Bean
  public MessageConsumer messageConsumer(@Value("${activemq.url}") String activeMQURL) {
    return new MessageConsumerActiveMQImpl(activeMQURL);
  }
}
