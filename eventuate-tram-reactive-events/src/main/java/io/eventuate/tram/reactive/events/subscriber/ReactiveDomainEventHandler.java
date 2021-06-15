package io.eventuate.tram.reactive.events.subscriber;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.common.EventMessageHeaders;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.messaging.common.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

public class ReactiveDomainEventHandler {
  private String aggregateType;
  private final Class<DomainEvent> eventClass;
  private final Function<DomainEventEnvelope<DomainEvent>, Publisher<?>> handler;

  public ReactiveDomainEventHandler(String aggregateType, Class<DomainEvent> eventClass, Function<DomainEventEnvelope<DomainEvent>, Publisher<?>> handler) {
    this.aggregateType = aggregateType;
    this.eventClass = eventClass;
    this.handler = handler;
  }

  public boolean handles(Message message) {
    return aggregateType.equals(message.getRequiredHeader(EventMessageHeaders.AGGREGATE_TYPE))
            && eventClass.getName().equals(message.getRequiredHeader(EventMessageHeaders.EVENT_TYPE));
  }

  public Publisher<?> invoke(DomainEventEnvelope<DomainEvent> dee) {
    return handler.apply(dee);
  }

  public Class<DomainEvent> getEventClass() {
    return eventClass;
  }

  public String getAggregateType() {
    return aggregateType;
  }
}
