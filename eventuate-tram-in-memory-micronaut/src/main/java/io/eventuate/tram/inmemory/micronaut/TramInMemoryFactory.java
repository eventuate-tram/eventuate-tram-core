package io.eventuate.tram.inmemory.micronaut;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.id.IdGeneratorImpl;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.inmemory.InMemoryMessageConsumer;
import io.eventuate.tram.inmemory.InMemoryMessageProducer;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.util.Collection;

@Factory
public class TramInMemoryFactory {

  @Singleton
  @Named("tramEmbeddedSchema")
  public EmbeddedSchema tramEmbeddedSchema() {
    return new EmbeddedSchema("eventuate-tram-embedded-schema.sql");
  }

  @Singleton
  public InMemoryMessageConsumer inMemoryMessageConsumer() {
    return new InMemoryMessageConsumer();
  }

  @Singleton
  @Primary
  public MessageConsumerImplementation messageConsumerImplementation(InMemoryMessageConsumer inMemoryMessageConsumer) {
    return inMemoryMessageConsumer;
  }

  @Singleton
  public InMemoryMessageProducer inMemoryMessageProducer(InMemoryMessageConsumer messageConsumer, IdGenerator idGenerator) {
    return new InMemoryMessageProducer(messageConsumer, idGenerator);
  }

  @Singleton
  @Primary
  public MessageProducerImplementation messageProducerImplementation(InMemoryMessageProducer inMemoryMessageProducer) {
    return inMemoryMessageProducer;
  }

  @Singleton
  @Primary
  public DataSource dataSource(Collection<EmbeddedSchema> schemas) {
    EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
    builder.setType(EmbeddedDatabaseType.H2);
    schemas.forEach(schema -> builder.addScript(schema.getResourcePath()));
    return builder.build();
  }

  @Singleton
  @Primary
  public IdGenerator idGenerator() {
    return new IdGeneratorImpl();
  }

  @Singleton
  @Requires(missingBeans = PlatformTransactionManager.class)
  public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }
}
