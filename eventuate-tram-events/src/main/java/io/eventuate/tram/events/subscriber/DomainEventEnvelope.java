package io.eventuate.tram.events.subscriber;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.messaging.common.Message;

public interface DomainEventEnvelope<T extends DomainEvent> {
  String getAggregateId();
  Message getMessage();
  String getAggregateType();
  String getEventId();

  T getEvent();
}
