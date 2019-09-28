package io.eventuate.tram.consumer.kafka.spring;

import io.eventuate.messaging.kafka.consumer.MessageConsumerKafkaImpl;
import io.eventuate.messaging.kafka.consumer.spring.MessageConsumerKafkaConfiguration;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.common.spring.TramConsumerCommonConfiguration;
import io.eventuate.tram.consumer.kafka.EventuateTramKafkaMessageConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramConsumerCommonConfiguration.class, MessageConsumerKafkaConfiguration.class})
public class EventuateTramKafkaMessageConsumerConfiguration {
  @Bean
  public MessageConsumerImplementation messageConsumerImplementation(MessageConsumerKafkaImpl messageConsumerKafka) {
    return new EventuateTramKafkaMessageConsumer(messageConsumerKafka);
  }
}
