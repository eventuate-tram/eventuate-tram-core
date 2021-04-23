package io.eventuate.tram.spring.events.publisher;

import io.eventuate.tram.events.common.DomainEventNameMapping;
import io.eventuate.tram.spring.messaging.producer.jdbc.reactive.SpringReactiveMessageProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactiveTramEventsPublisherConfiguration {

  @Bean
  public ReactiveDomainEventPublisher reactiveDomainEventPublisher(SpringReactiveMessageProducer reactiveMessageProducer,
                                                                   DomainEventNameMapping domainEventNameMapping) {
    return new ReactiveDomainEventPublisher(reactiveMessageProducer, domainEventNameMapping);
  }
}
