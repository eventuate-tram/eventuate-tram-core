package io.eventuate.tram.micronaut.events.publisher;

import io.eventuate.tram.events.common.DomainEventNameMapping;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.events.publisher.DomainEventPublisherImpl;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.micronaut.context.annotation.Factory;

import jakarta.inject.Singleton;

@Factory
public class TramEventsPublisherFactory {

  @Singleton
  public DomainEventPublisher domainEventPublisher(MessageProducer messageProducer, DomainEventNameMapping domainEventNameMapping) {
    return new DomainEventPublisherImpl(messageProducer, domainEventNameMapping);
  }
}
