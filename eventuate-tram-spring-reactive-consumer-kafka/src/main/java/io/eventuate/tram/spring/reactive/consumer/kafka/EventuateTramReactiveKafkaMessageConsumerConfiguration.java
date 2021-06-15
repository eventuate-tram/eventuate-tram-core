package io.eventuate.tram.spring.reactive.consumer.kafka;

import io.eventuate.messaging.kafka.consumer.MessageConsumerKafkaImpl;
import io.eventuate.messaging.kafka.spring.consumer.KafkaConsumerFactoryConfiguration;
import io.eventuate.messaging.kafka.spring.consumer.MessageConsumerKafkaConfiguration;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumerImplementation;
import io.eventuate.tram.reactive.consumer.kafka.EventuateTramReactiveKafkaMessageConsumer;
import io.eventuate.tram.spring.reactive.consumer.common.ReactiveTramConsumerBaseCommonConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ReactiveTramConsumerBaseCommonConfiguration.class, MessageConsumerKafkaConfiguration.class, KafkaConsumerFactoryConfiguration.class})
public class EventuateTramReactiveKafkaMessageConsumerConfiguration {

  @Bean
  public ReactiveMessageConsumerImplementation messageConsumerImplementation(MessageConsumerKafkaImpl messageConsumerKafka) {
    return new EventuateTramReactiveKafkaMessageConsumer(messageConsumerKafka);
  }
}
