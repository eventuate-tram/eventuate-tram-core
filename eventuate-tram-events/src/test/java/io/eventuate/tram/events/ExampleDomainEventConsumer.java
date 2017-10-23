package io.eventuate.tram.events;

import io.eventuate.tram.events.subscriber.DomainEventEnvelope;

public class ExampleDomainEventConsumer {

  public void handleEvent(DomainEventEnvelope<ExampleDomainEvent> event) {
    System.out.println("I got an event: " + event);
  }
}
