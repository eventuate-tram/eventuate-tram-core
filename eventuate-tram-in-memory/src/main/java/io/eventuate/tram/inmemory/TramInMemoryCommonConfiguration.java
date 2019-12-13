package io.eventuate.tram.inmemory;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.id.IdGeneratorImpl;
import io.eventuate.common.inmemorydatabase.EventuateCommonInMemoryDatabaseConfiguration;
import io.eventuate.common.inmemorydatabase.EventuateDatabaseScriptSupplier;
import io.eventuate.tram.consumer.common.spring.TramConsumerCommonConfiguration;
import io.eventuate.tram.messaging.producer.common.TramMessagingCommonProducerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collections;

@Configuration
@Import({TramConsumerCommonConfiguration.class,
        TramMessagingCommonProducerConfiguration.class,
        EventuateCommonInMemoryDatabaseConfiguration.class})
public class TramInMemoryCommonConfiguration {


  @Bean
  public InMemoryMessageConsumer inMemoryMessageConsumer() {
    return new InMemoryMessageConsumer();
  }

  @Bean
  public InMemoryMessageProducer inMemoryMessageProducer(InMemoryMessageConsumer messageConsumer, IdGenerator idGenerator) {
    return new InMemoryMessageProducer(messageConsumer, idGenerator);
  }

  @Bean
  public EventuateDatabaseScriptSupplier eventuateCommonInMemoryScriptSupplierForTram() {
    return () -> Collections.singletonList("eventuate-tram-embedded-schema.sql");
  }

  @Bean
  public IdGenerator idGenerator() {
    return new IdGeneratorImpl();
  }

}
