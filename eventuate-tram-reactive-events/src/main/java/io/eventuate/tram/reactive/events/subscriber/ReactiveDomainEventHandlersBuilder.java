package io.eventuate.tram.reactive.events.subscriber;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.messaging.common.Message;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ReactiveDomainEventHandlersBuilder {
  private String aggregateType;
  private List<ReactiveDomainEventHandler> handlers = new ArrayList<>();

  public ReactiveDomainEventHandlersBuilder(String aggregateType) {
    this.aggregateType = aggregateType;
  }

  public static ReactiveDomainEventHandlersBuilder forAggregateType(String aggregateType) {
    return new ReactiveDomainEventHandlersBuilder(aggregateType);
  }

  public <E extends DomainEvent> ReactiveDomainEventHandlersBuilder onEvent(Class<E> eventClass, Function<DomainEventEnvelope<E>, Mono<Void>> handler) {

    Function<DomainEventEnvelope<E>, Mono<Message>> convertedHandler = dee -> handler.apply(dee).flatMap(unused -> Mono.just(dee.getMessage()));

    handlers.add(new ReactiveDomainEventHandler(aggregateType, ((Class<DomainEvent>) eventClass), (e) -> convertedHandler.apply((DomainEventEnvelope<E>) e)));
    return this;
  }

  public ReactiveDomainEventHandlersBuilder andForAggregateType(String aggregateType) {
    this.aggregateType = aggregateType;
    return this;
  }

  public ReactiveDomainEventHandlers build() {
    return new ReactiveDomainEventHandlers(handlers);
  }
}
