package io.eventuate.tram.messaging.producer.jdbc;

import io.eventuate.javaclient.spring.jdbc.IdGenerator;
import io.eventuate.javaclient.spring.jdbc.IdGeneratorImpl;
import io.eventuate.local.common.EventuateConstants;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TramMessageProducerJdbcConfiguration {

  @Value("${eventuate.database.schema:#{\"" + EventuateConstants.DEFAULT_DATABASE_SCHEMA + "\"}}")
  private String eventuateDatabaseSchema;

  @Bean
  public MessageProducer messageProducer() {
    return new MessageProducerJdbcImpl(eventuateDatabaseSchema);
  }

  @Bean
  public IdGenerator idGenerator() {
    return new IdGeneratorImpl();
  }
}
