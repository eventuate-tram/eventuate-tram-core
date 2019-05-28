package io.eventuate.tram.events.common;

public class DefaultDomainEventNameMapping implements DomainEventNameMapping {

  @Override
  public String eventToExternalEventType(String aggregateType, DomainEvent event) {
    return event.getClass().getName();
  }

  @Override
  public String externalEventTypeToEventClassName(String aggregateType, String eventTypeHeader) {
    return eventTypeHeader;
  }

}
