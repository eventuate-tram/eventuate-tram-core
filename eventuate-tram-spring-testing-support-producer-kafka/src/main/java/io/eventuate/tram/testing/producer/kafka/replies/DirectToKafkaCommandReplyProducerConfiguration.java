package io.eventuate.tram.testing.producer.kafka.replies;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DirectToKafkaCommandReplyProducerConfiguration {

  @Bean
  public DirectToKafkaCommandReplyProducer directToKafkaCommandReplyProducer(
      @Value("${eventuatelocal.kafka.bootstrap.servers}") String bootstrapServers) {
    return new DirectToKafkaCommandReplyProducer(bootstrapServers);
  }
}
