package io.eventuate.tram.messaging.producer.jdbc;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.javaclient.spring.jdbc.IdGenerator;
import io.eventuate.javaclient.spring.jdbc.IdGeneratorImpl;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TramMessageProducerJdbcConfiguration {

  @Bean
  public EventuateSchema eventuateSchema(@Value("${eventuate.database.schema:#{null}}") String eventuateDatabaseSchema) {
    return new EventuateSchema(eventuateDatabaseSchema);
  }

  @Bean
  public MessageProducer messageProducer(EventuateSchema eventuateSchema) {
    return new MessageProducerJdbcImpl(eventuateSchema);
  }

  @Bean
  public IdGenerator idGenerator() {
    return new IdGeneratorImpl();
  }
}
