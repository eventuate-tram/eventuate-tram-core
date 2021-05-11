package io.eventuate.tram.reactive.integrationtests;

import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.reactive.events.subscriber.ReactiveDomainEventHandlers;
import io.eventuate.tram.reactive.events.subscriber.ReactiveDomainEventHandlersBuilder;
import reactor.core.publisher.Mono;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ReactiveTramTestEventConsumer {

  private BlockingQueue<TestEvent> queue = new LinkedBlockingDeque<>();
  private String aggregateType;

  public ReactiveTramTestEventConsumer(String aggregateType) {
    this.aggregateType = aggregateType;
  }

  public ReactiveDomainEventHandlers domainEventHandlers() {
    return ReactiveDomainEventHandlersBuilder
            .forAggregateType(aggregateType)
            .onEvent(TestEvent.class, this::handleTestEvent)
            .build();
  }

  public String getAggregateType() {
    return aggregateType;
  }

  public Mono<Void> handleTestEvent(DomainEventEnvelope<TestEvent> event) {
    queue.add(event.getEvent());

    return Mono.empty();
  }

  public BlockingQueue<TestEvent> getQueue() {
    return queue;
  }
}
