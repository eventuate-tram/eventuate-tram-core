package io.eventuate.tram.testing.producer.kafka.commands;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DirectToKafkaCommandProducerConfiguration {

  @Bean
  public DirectToKafkaCommandProducer directToKafkaCommandProducer(
      @Value("${eventuatelocal.kafka.bootstrap.servers}") String bootstrapServers) {
    return new DirectToKafkaCommandProducer(bootstrapServers);
  }
}
