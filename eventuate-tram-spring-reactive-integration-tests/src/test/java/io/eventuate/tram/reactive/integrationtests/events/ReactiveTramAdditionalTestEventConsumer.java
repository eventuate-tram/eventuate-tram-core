package io.eventuate.tram.reactive.integrationtests.events;

import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.reactive.events.subscriber.ReactiveDomainEventHandlers;
import io.eventuate.tram.reactive.events.subscriber.ReactiveDomainEventHandlersBuilder;
import io.eventuate.tram.reactive.integrationtests.events.AdditionalTestEvent;
import reactor.core.publisher.Mono;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ReactiveTramAdditionalTestEventConsumer {
  private BlockingQueue<AdditionalTestEvent> queue = new LinkedBlockingDeque<>();
  private String aggregateType;

  public ReactiveTramAdditionalTestEventConsumer(String aggregateType) {
    this.aggregateType = aggregateType;
  }

  public ReactiveDomainEventHandlers domainEventHandlers() {
    return ReactiveDomainEventHandlersBuilder
            .forAggregateType(aggregateType)
            .onEvent(AdditionalTestEvent.class, this::handleTestEvent)
            .build();
  }

  public String getAggregateType() {
    return aggregateType;
  }

  public Mono<?> handleTestEvent(DomainEventEnvelope<AdditionalTestEvent> event) {
    return Mono.defer(() -> Mono.just(queue.add(event.getEvent())));
  }

  public BlockingQueue<AdditionalTestEvent> getQueue() {
    return queue;
  }
}
