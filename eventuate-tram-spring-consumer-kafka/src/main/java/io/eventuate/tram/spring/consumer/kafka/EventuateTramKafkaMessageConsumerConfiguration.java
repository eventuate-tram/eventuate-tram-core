package io.eventuate.tram.spring.consumer.kafka;

import io.eventuate.messaging.kafka.consumer.MessageConsumerKafkaImpl;
import io.eventuate.messaging.kafka.spring.consumer.KafkaConsumerFactoryConfiguration;
import io.eventuate.messaging.kafka.spring.consumer.MessageConsumerKafkaConfiguration;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.spring.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.consumer.kafka.EventuateTramKafkaMessageConsumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramConsumerCommonConfiguration.class, MessageConsumerKafkaConfiguration.class, KafkaConsumerFactoryConfiguration.class})
public class EventuateTramKafkaMessageConsumerConfiguration {

  @Bean
  public MessageConsumerImplementation messageConsumerImplementation(MessageConsumerKafkaImpl messageConsumerKafka) {
    return new EventuateTramKafkaMessageConsumer(messageConsumerKafka);
  }
}
