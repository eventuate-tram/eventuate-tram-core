package io.eventuate.tram.spring.testing.outbox.events;

import io.eventuate.tram.spring.testing.outbox.messaging.MessageOutboxTestSupport;
import io.eventuate.tram.spring.testing.outbox.messaging.MessageOutboxTestSupportConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(MessageOutboxTestSupportConfiguration.class)
public class DomainEventOutboxTestSupportConfiguration {

  @Bean
  public DomainEventOutboxTestSupport domainEventOutboxTestSupport(MessageOutboxTestSupport messageOutboxTestSupport) {
    return new DomainEventOutboxTestSupport(messageOutboxTestSupport);
  }
}
