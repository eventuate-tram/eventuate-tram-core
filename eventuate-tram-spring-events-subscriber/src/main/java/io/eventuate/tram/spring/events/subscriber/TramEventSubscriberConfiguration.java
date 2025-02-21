package io.eventuate.tram.spring.events.subscriber;

import io.eventuate.tram.events.common.DomainEventNameMapping;
import io.eventuate.tram.events.subscriber.DomainEventDispatcherFactory;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(AnnotationBasedEventsSubscriberConfiguration.class)
public class TramEventSubscriberConfiguration {

  @Bean
  public DomainEventDispatcherFactory domainEventDispatcherFactory(MessageConsumer messageConsumer, DomainEventNameMapping domainEventNameMapping) {
    return new DomainEventDispatcherFactory(messageConsumer, domainEventNameMapping);
  }
}
