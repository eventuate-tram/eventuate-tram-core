package io.eventuate.tram.events.subscriber;

import io.eventuate.tram.events.common.DomainEventNameMapping;
import io.eventuate.tram.messaging.consumer.MessageConsumer;

public class DomainEventDispatcherFactory {

  protected MessageConsumer messageConsumer;
  protected DomainEventNameMapping domainEventNameMapping;

  public DomainEventDispatcherFactory(MessageConsumer messageConsumer, DomainEventNameMapping domainEventNameMapping) {
    this.messageConsumer = messageConsumer;
    this.domainEventNameMapping = domainEventNameMapping;
  }

  public DomainEventDispatcher make(String eventDispatcherId, DomainEventHandlers domainEventHandlers) {
    return new DomainEventDispatcher(eventDispatcherId, domainEventHandlers, messageConsumer, domainEventNameMapping);
  }
}
