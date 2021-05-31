package io.eventuate.tram.reactive.integrationtests;

import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.reactive.events.subscriber.ReactiveDomainEventHandlers;
import io.eventuate.tram.reactive.events.subscriber.ReactiveDomainEventHandlersBuilder;
import io.eventuate.tram.spring.events.publisher.ReactiveDomainEventPublisher;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ReactiveTramTestEventConsumer {
  private ReactiveDomainEventPublisher domainEventPublisher;
  private BlockingQueue<TestEvent> queue = new LinkedBlockingDeque<>();
  private String aggregateType;

  public ReactiveTramTestEventConsumer(String aggregateType, ReactiveDomainEventPublisher domainEventPublisher) {
    this.aggregateType = aggregateType;
    this.domainEventPublisher = domainEventPublisher;
  }

  public ReactiveDomainEventHandlers domainEventHandlers() {
    return ReactiveDomainEventHandlersBuilder
            .forAggregateType(aggregateType)
            .onEvent(TestEvent.class, this::handleTestEvent)
            .onEvent(TestEventThatInitiatesException.class, this::handleEventThatInitiatesException)
            .build();
  }

  public String getAggregateType() {
    return aggregateType;
  }

  public Mono<Void> handleTestEvent(DomainEventEnvelope<TestEvent> event) {
    queue.add(event.getEvent());

    return Mono.empty();
  }

  public Mono<Void> handleEventThatInitiatesException(DomainEventEnvelope<TestEventThatInitiatesException> event) {
    return domainEventPublisher
            // consumer tries to publish reply
            .publish(aggregateType, event.getAggregateId(), Collections.singletonList(new TestEvent(event.getEvent().getPayload())))
            // something went wrong and reply message should not be inserted into database
            .map(message -> {
              throw new RuntimeException("Something happened");
            })
            .then();
  }

  public BlockingQueue<TestEvent> getQueue() {
    return queue;
  }
}
