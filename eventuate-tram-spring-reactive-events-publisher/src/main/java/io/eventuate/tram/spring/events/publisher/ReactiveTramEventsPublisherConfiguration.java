package io.eventuate.tram.spring.events.publisher;

import io.eventuate.tram.events.common.DomainEventNameMapping;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactiveTramEventsPublisherConfiguration {

  @Bean
  public ReactiveDomainEventPublisher reactiveDomainEventPublisher(ReactiveMessageProducer reactiveMessageProducer,
                                                                   DomainEventNameMapping domainEventNameMapping) {
    return new ReactiveDomainEventPublisher(reactiveMessageProducer, domainEventNameMapping);
  }
}
