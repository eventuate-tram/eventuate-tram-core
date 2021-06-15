package io.eventuate.tram.reactive.events.subscriber;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.events.common.DomainEventNameMapping;

public class ReactiveDomainEventDispatcherFactory {

  protected ReactiveMessageConsumer messageConsumer;
  protected DomainEventNameMapping domainEventNameMapping;

  public ReactiveDomainEventDispatcherFactory(ReactiveMessageConsumer messageConsumer, DomainEventNameMapping domainEventNameMapping) {
    this.messageConsumer = messageConsumer;
    this.domainEventNameMapping = domainEventNameMapping;
  }

  public ReactiveDomainEventDispatcher make(String eventDispatcherId, ReactiveDomainEventHandlers domainEventHandlers) {
    return new ReactiveDomainEventDispatcher(eventDispatcherId, domainEventHandlers, messageConsumer, domainEventNameMapping);
  }
}
