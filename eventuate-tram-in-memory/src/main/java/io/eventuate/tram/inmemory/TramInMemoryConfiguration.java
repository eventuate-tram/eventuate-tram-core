package io.eventuate.tram.inmemory;

import io.eventuate.javaclient.spring.jdbc.IdGenerator;
import io.eventuate.javaclient.spring.jdbc.IdGeneratorImpl;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Configuration
public class TramInMemoryConfiguration {

  @Autowired(required = false)
  private MessageInterceptor[] messageInterceptors = new MessageInterceptor[0];

  @Bean
  public InMemoryMessaging inMemoryMessaging() {
    return new InMemoryMessaging(messageInterceptors);
  }

  @Bean
  public MessageProducer messageProducer(InMemoryMessaging inMemoryMessaging) {
    return inMemoryMessaging;
  }

  @Bean
  public MessageConsumer messageConsumer(InMemoryMessaging inMemoryMessaging) {
    return inMemoryMessaging;
  }

  @Bean
  public DataSource dataSource() {
    EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
    return builder.setType(EmbeddedDatabaseType.H2).addScript("eventuate-tram-embedded-schema.sql").build();
  }

  @Bean
  public IdGenerator idGenerator() {
    return new IdGeneratorImpl();
  }
}
