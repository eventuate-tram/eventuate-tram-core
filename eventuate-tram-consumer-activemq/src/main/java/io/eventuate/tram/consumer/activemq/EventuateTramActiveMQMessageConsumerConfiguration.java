package io.eventuate.tram.consumer.activemq;

import io.eventuate.messaging.activemq.consumer.MessageConsumerActiveMQImpl;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventuateTramActiveMQMessageConsumerConfiguration {
  @Bean
  public MessageConsumerImplementation messageConsumerImplementation(MessageConsumerActiveMQImpl messageConsumerActiveMQ) {
    return new EventuateTramActiveMQMessageConsumer(messageConsumerActiveMQ);
  }
}
