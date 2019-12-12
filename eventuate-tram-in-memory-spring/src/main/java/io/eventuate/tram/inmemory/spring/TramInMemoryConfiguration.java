package io.eventuate.tram.inmemory.spring;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.tram.consumer.jdbc.spring.TransactionalNoopDuplicateMessageDetectorConfiguration;
import io.eventuate.tram.inmemory.InMemoryMessageConsumer;
import io.eventuate.tram.inmemory.InMemoryMessageProducer;
import io.eventuate.tram.inmemory.TramInMemoryCommonConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramInMemoryCommonConfiguration.class, TransactionalNoopDuplicateMessageDetectorConfiguration.class, })
public class TramInMemoryConfiguration {

  @Bean
  public InMemoryMessageConsumer inMemoryMessageConsumer() {
    return new InMemoryMessageConsumer();
  }

  @Bean
  public InMemoryMessageProducer inMemoryMessageProducer(InMemoryMessageConsumer messageConsumer, IdGenerator idGenerator) {
    return new InMemoryMessageProducer(messageConsumer, idGenerator);
  }
}
