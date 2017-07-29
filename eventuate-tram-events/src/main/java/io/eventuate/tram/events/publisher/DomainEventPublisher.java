package io.eventuate.tram.events.publisher;

import io.eventuate.tram.events.common.DomainEvent;

import java.util.List;
import java.util.Map;

public interface DomainEventPublisher {

  void publish(String aggregateType, String aggregateId, List<DomainEvent> domainEvents);
  void publish(String aggregateType, String aggregateId, Map<String, String> headers, List<DomainEvent> domainEvents);

  default void publish(Class<?> aggregateType, String aggregateId, List<DomainEvent> domainEvents) {
    publish(aggregateType.getName(), aggregateId, domainEvents);
  }

//  void publish(String destination,  Map<String, String> headers, List<DomainEvent> domainEvents);
}
