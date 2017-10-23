package io.eventuate.tram.events;

import io.eventuate.tram.events.subscriber.DomainEventEnvelope;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MyEventHandler {

  private BlockingQueue<DomainEventEnvelope<ExampleDomainEvent>> receivedEvents = new LinkedBlockingQueue<>();

  public BlockingQueue<DomainEventEnvelope<ExampleDomainEvent>> getReceivedEvents() {
    return receivedEvents;
  }

  public void handleEvent(DomainEventEnvelope<ExampleDomainEvent> de) {
    System.out.println(de);
    receivedEvents.add(de);
  }
}
