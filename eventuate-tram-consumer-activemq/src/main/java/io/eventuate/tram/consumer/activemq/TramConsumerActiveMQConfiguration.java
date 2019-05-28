package io.eventuate.tram.consumer.activemq;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Optional;

@Configuration
@Import(TramConsumerCommonConfiguration.class)
public class TramConsumerActiveMQConfiguration {

  @Bean
  public MessageConsumerImplementation messageConsumerImplementation(@Value("${activemq.url}") String activeMQURL,
                                         @Value("${activemq.user:#{null}}") String activeMQUser,
                                         @Value("${activemq.password:#{null}}") String activeMQPassword) {
    return new MessageConsumerActiveMQImpl(activeMQURL, Optional.ofNullable(activeMQUser), Optional.ofNullable(activeMQPassword));
  }

}
