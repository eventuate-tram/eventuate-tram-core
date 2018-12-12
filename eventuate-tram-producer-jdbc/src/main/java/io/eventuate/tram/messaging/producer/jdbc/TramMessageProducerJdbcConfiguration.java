package io.eventuate.tram.messaging.producer.jdbc;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.javaclient.spring.jdbc.IdGenerator;
import io.eventuate.javaclient.spring.jdbc.IdGeneratorImpl;
import io.eventuate.tram.messaging.common.CommonMessagingConfiguration;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CommonMessagingConfiguration.class)
public class TramMessageProducerJdbcConfiguration {

  @Autowired(required = false)
  private MessageInterceptor[] messageInterceptors = new MessageInterceptor[0];

  @Bean
  public MessageProducer messageProducer(EventuateSchema eventuateSchema) {
    return new MessageProducerJdbcImpl(eventuateSchema, messageInterceptors);
  }

  @Bean
  public IdGenerator idGenerator() {
    return new IdGeneratorImpl();
  }
}
