package io.eventuate.tram.events.common;

/**
 * Maps between event class names and external event types
 */
public interface DomainEventNameMapping {

  String eventToExternalEventType(String aggregateType, DomainEvent event);
  String externalEventTypeToEventClassName(String aggregateType, String eventTypeHeader);
}
