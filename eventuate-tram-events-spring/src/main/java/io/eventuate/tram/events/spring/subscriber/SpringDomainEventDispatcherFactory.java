package io.eventuate.tram.events.spring.subscriber;

import io.eventuate.tram.events.common.DomainEventNameMapping;
import io.eventuate.tram.events.subscriber.DomainEventDispatcher;
import io.eventuate.tram.events.subscriber.DomainEventDispatcherFactory;
import io.eventuate.tram.events.subscriber.DomainEventHandlers;
import io.eventuate.tram.messaging.consumer.MessageConsumer;

public class SpringDomainEventDispatcherFactory extends DomainEventDispatcherFactory {

  public SpringDomainEventDispatcherFactory(MessageConsumer messageConsumer, DomainEventNameMapping domainEventNameMapping) {
    super(messageConsumer, domainEventNameMapping);
  }

  @Override
  public DomainEventDispatcher make(String eventDispatcherId, DomainEventHandlers domainEventHandlers) {
    return new SpringDomainEventDispatcher(eventDispatcherId, domainEventHandlers, messageConsumer, domainEventNameMapping);
  }
}
