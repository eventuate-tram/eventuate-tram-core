package io.eventuate.tram.events.spring.subscriber;

import io.eventuate.tram.events.common.DomainEventNameMapping;
import io.eventuate.tram.events.subscriber.DomainEventDispatcher;
import io.eventuate.tram.events.subscriber.DomainEventHandlers;
import io.eventuate.tram.messaging.consumer.MessageConsumer;

import javax.annotation.PostConstruct;

public class SpringDomainEventDispatcher extends DomainEventDispatcher {

  public SpringDomainEventDispatcher(String eventDispatcherId,
                                     DomainEventHandlers domainEventHandlers,
                                     MessageConsumer messageConsumer,
                                     DomainEventNameMapping domainEventNameMapping) {
    super(eventDispatcherId, domainEventHandlers, messageConsumer, domainEventNameMapping);
  }

  @Override
  @PostConstruct
  public void initialize() {
    super.initialize();
  }
}
