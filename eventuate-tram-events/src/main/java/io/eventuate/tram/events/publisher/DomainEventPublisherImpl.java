package io.eventuate.tram.events.publisher;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.common.DomainEventNameMapping;
import io.eventuate.tram.events.common.EventUtil;
import io.eventuate.tram.messaging.producer.MessageProducer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DomainEventPublisherImpl implements DomainEventPublisher {

  private MessageProducer messageProducer;

  private DomainEventNameMapping domainEventNameMapping;

  public DomainEventPublisherImpl(MessageProducer messageProducer, DomainEventNameMapping domainEventNameMapping) {
    this.messageProducer = messageProducer;
    this.domainEventNameMapping = domainEventNameMapping;
  }

  @Override
  public void publish(String aggregateType, Object aggregateId, List<DomainEvent> domainEvents) {
    publish(aggregateType, aggregateId, Collections.emptyMap(), domainEvents);
  }

  @Override
  public void publish(String aggregateType,
                      Object aggregateId,
                      Map<String, String> headers,
                      List<DomainEvent> domainEvents) {

    for (DomainEvent event : domainEvents) {
      messageProducer.send(aggregateType,
              EventUtil.makeMessageForDomainEvent(aggregateType, aggregateId, headers, event,
                      domainEventNameMapping.eventToExternalEventType(aggregateType, event)));

    }
  }
}
