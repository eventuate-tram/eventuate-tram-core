package io.eventuate.tram.spring.reactive.events.subscriber;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.events.common.DomainEventNameMapping;
import io.eventuate.tram.reactive.events.subscriber.ReactiveDomainEventDispatcher;
import io.eventuate.tram.reactive.events.subscriber.ReactiveDomainEventDispatcherFactory;
import io.eventuate.tram.reactive.events.subscriber.ReactiveDomainEventHandlers;

public class SpringReactiveDomainEventDispatcherFactory extends ReactiveDomainEventDispatcherFactory {

  public SpringReactiveDomainEventDispatcherFactory(ReactiveMessageConsumer messageConsumer, DomainEventNameMapping domainEventNameMapping) {
    super(messageConsumer, domainEventNameMapping);
  }

  @Override
  public ReactiveDomainEventDispatcher make(String eventDispatcherId, ReactiveDomainEventHandlers domainEventHandlers) {
    ReactiveDomainEventDispatcher reactiveDomainEventDispatcher = new ReactiveDomainEventDispatcher(eventDispatcherId, domainEventHandlers, messageConsumer, domainEventNameMapping);
    reactiveDomainEventDispatcher.initialize();
    return reactiveDomainEventDispatcher;
  }
}
