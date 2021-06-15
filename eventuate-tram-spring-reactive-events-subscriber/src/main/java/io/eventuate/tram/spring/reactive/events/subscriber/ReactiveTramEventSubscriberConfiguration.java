package io.eventuate.tram.spring.reactive.events.subscriber;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.events.common.DomainEventNameMapping;
import io.eventuate.tram.reactive.events.subscriber.ReactiveDomainEventDispatcherFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactiveTramEventSubscriberConfiguration {

  @Bean
  public ReactiveDomainEventDispatcherFactory domainEventDispatcherFactory(ReactiveMessageConsumer messageConsumer,
                                                                           DomainEventNameMapping domainEventNameMapping) {
    return new SpringReactiveDomainEventDispatcherFactory(messageConsumer, domainEventNameMapping);
  }
}
