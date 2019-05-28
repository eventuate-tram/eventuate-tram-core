package io.eventuate.tram.events.subscriber;

import io.eventuate.tram.events.common.DomainEventNameMapping;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TramEventSubscriberConfiguration {

  @Bean
  public DomainEventDispatcherFactory domainEventDispatcherFactory(MessageConsumer messageConsumer, DomainEventNameMapping domainEventNameMapping) {
    return new DomainEventDispatcherFactory(messageConsumer, domainEventNameMapping);
  }
}
