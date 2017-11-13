package io.eventuate.tram.messaging.producer.jdbc;

import io.eventuate.javaclient.spring.jdbc.IdGenerator;
import io.eventuate.javaclient.spring.jdbc.IdGeneratorImpl;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class TramMessageProducerJdbcConfiguration {

  @Value("${eventuateLocal.cdc.eventuate.database:#{\"eventuate\"}}")
  private String eventuateDatabase;

  @Bean
  public MessageProducer messageProducer() {
    return new MessageProducerJdbcImpl(eventuateDatabase);
  }

  @Bean
  public IdGenerator idGenerator() {
    return new IdGeneratorImpl();
  }
}
