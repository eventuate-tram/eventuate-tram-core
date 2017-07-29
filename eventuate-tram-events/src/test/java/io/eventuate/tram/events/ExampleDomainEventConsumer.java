package io.eventuate.tram.events;

import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.DomainEventHandler;

public class ExampleDomainEventConsumer {

  @DomainEventHandler
  public void handleEvent(DomainEventEnvelope<ExampleDomainEvent> event) {
    System.out.println("I got an event: " + event);
  }
}
