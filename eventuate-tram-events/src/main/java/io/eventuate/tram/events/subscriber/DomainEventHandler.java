package io.eventuate.tram.events.subscriber;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.common.EventMessageHeaders;
import io.eventuate.tram.messaging.common.Message;

import java.util.function.Consumer;

public class DomainEventHandler {
  private String aggregateType;
  private final Class<DomainEvent> eventClass;
  private final Consumer<DomainEventEnvelope<DomainEvent>> handler;

  public DomainEventHandler(String aggregateType, Class<DomainEvent> eventClass, Consumer<DomainEventEnvelope<DomainEvent>> handler) {
    this.aggregateType = aggregateType;
    this.eventClass = eventClass;
    this.handler = handler;
  }

  public boolean handles(Message message) {
    return aggregateType.equals(message.getRequiredHeader(EventMessageHeaders.AGGREGATE_TYPE))
            && eventClass.getName().equals(message.getRequiredHeader(EventMessageHeaders.EVENT_TYPE));
  }

  public void invoke(DomainEventEnvelope<DomainEvent> dee) {
    handler.accept(dee);
  }

  public Class<DomainEvent> getEventClass() {
    return eventClass;
  }

  public String getAggregateType() {
    return aggregateType;
  }
}
