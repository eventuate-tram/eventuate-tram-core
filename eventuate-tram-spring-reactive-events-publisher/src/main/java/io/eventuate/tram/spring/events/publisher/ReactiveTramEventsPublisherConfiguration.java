package io.eventuate.tram.spring.events.publisher;

import io.eventuate.tram.events.common.DomainEventNameMapping;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.events.publisher.DomainEventPublisherImpl;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactiveTramEventsPublisherConfiguration {

  @Bean
  public DomainEventPublisher domainEventPublisher(MessageProducer messageProducer, DomainEventNameMapping domainEventNameMapping) {
    return new DomainEventPublisherImpl(messageProducer, domainEventNameMapping);
  }
}
