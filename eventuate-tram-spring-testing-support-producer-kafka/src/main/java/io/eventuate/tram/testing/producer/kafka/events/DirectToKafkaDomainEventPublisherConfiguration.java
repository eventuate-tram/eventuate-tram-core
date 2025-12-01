package io.eventuate.tram.testing.producer.kafka.events;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DirectToKafkaDomainEventPublisherConfiguration {

  @Bean
  public DirectToKafkaDomainEventPublisher directToKafkaDomainEventPublisher(
      @Value("${eventuatelocal.kafka.bootstrap.servers}") String bootstrapServers) {
    return new DirectToKafkaDomainEventPublisher(bootstrapServers);
  }
}
