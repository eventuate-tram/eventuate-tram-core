package io.eventuate.tram.events.spring.subscriber;

import io.eventuate.tram.events.common.DomainEventNameMapping;
import io.eventuate.tram.events.subscriber.DomainEventDispatcherFactory;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TramEventSubscriberConfiguration {

  @Bean
  public DomainEventDispatcherFactory domainEventDispatcherFactory(MessageConsumer messageConsumer, DomainEventNameMapping domainEventNameMapping) {
    return new SpringDomainEventDispatcherFactory(messageConsumer, domainEventNameMapping);
  }
}
