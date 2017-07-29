package io.eventuate.tram.messaging.producer.jdbc;

import io.eventuate.javaclient.spring.jdbc.IdGenerator;
import io.eventuate.javaclient.spring.jdbc.IdGeneratorImpl;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TramMessageProducerJdbcConfiguration {

  @Bean
  public MessageProducer messageProducer() {
    return new MessageProducerJdbcImpl();
  }

  @Bean
  public IdGenerator idGenerator() {
    return new IdGeneratorImpl();
  }
}
