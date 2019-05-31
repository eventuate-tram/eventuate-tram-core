package io.eventuate.tram.inmemory;

import io.eventuate.common.id.generator.IdGenerator;
import io.eventuate.common.id.generator.IdGeneratorImpl;
import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.consumer.jdbc.TransactionalNoopDuplicateMessageDetectorConfiguration;
import io.eventuate.tram.messaging.producer.common.TramMessagingCommonProducerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Configuration
@Import({TramConsumerCommonConfiguration.class, TransactionalNoopDuplicateMessageDetectorConfiguration.class, TramMessagingCommonProducerConfiguration.class})
public class TramInMemoryConfiguration {

  @Bean
  public InMemoryMessageConsumer inMemoryMessageConsumer() {
    return new InMemoryMessageConsumer();
  }

  @Bean
  public InMemoryMessageProducer inMemoryMessageProducer(InMemoryMessageConsumer messageConsumer) {
    return new InMemoryMessageProducer(messageConsumer);
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
