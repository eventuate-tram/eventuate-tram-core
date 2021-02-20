package io.eventuate.tram.micronaut.inmemory;

import io.eventuate.common.inmemorydatabase.EventuateDatabaseScriptSupplier;
import io.eventuate.tram.common.spring.inmemory.EventuateSpringTransactionSynchronizationManager;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.inmemory.InMemoryMessageConsumer;
import io.eventuate.tram.inmemory.InMemoryMessageProducer;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.util.Collections;

@Factory
public class TramInMemoryFactory {

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
  public InMemoryMessageProducer inMemoryMessageProducer(InMemoryMessageConsumer messageConsumer) {
    return new InMemoryMessageProducer(messageConsumer, new EventuateSpringTransactionSynchronizationManager());
  }

  @Singleton
  @Primary
  public MessageProducerImplementation messageProducerImplementation(InMemoryMessageProducer inMemoryMessageProducer) {
    return inMemoryMessageProducer;
  }

  @Singleton
  @Named("TramEventuateDatabaseScriptSupplier")
  public EventuateDatabaseScriptSupplier eventuateCommonInMemoryScriptSupplierForTram() {
    return () -> Collections.singletonList("eventuate-tram-embedded-schema.sql");
  }

  @Singleton
  @Requires(missingBeans = PlatformTransactionManager.class)
  public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }
}
