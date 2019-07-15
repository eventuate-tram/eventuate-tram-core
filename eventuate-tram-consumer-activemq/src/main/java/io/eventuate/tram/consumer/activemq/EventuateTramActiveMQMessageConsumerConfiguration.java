package io.eventuate.tram.consumer.activemq;

import io.eventuate.messaging.activemq.consumer.MessageConsumerActiveMQConfiguration;
import io.eventuate.messaging.activemq.consumer.MessageConsumerActiveMQImpl;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MessageConsumerActiveMQConfiguration.class, TramConsumerCommonConfiguration.class,})
public class EventuateTramActiveMQMessageConsumerConfiguration {
  @Bean
  public MessageConsumerImplementation messageConsumerImplementation(MessageConsumerActiveMQImpl messageConsumerActiveMQ) {
    return new EventuateTramActiveMQMessageConsumer(messageConsumerActiveMQ);
  }
}
