package io.eventuate.tram.spring.events.publisher;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.common.DomainEventNameMapping;
import io.eventuate.tram.events.common.EventUtil;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.function.Function.identity;

public class ReactiveDomainEventPublisher {

  private ReactiveMessageProducer reactiveMessageProducer;
  private DomainEventNameMapping domainEventNameMapping;

  public ReactiveDomainEventPublisher(ReactiveMessageProducer reactiveMessageProducer, DomainEventNameMapping domainEventNameMapping) {
    this.reactiveMessageProducer = reactiveMessageProducer;
    this.domainEventNameMapping = domainEventNameMapping;
  }

  public Flux<Message> publish(String aggregateType, Object aggregateId, List<DomainEvent> domainEvents) {
    return publish(aggregateType, aggregateId, Collections.emptyMap(), domainEvents);
  }

  public Flux<Message> publish(String aggregateType, Object aggregateId, Map<String, String> headers, List<DomainEvent> domainEvents) {
    Stream<Mono<Message>> messages = domainEvents
            .stream()
            .map(event -> reactiveMessageProducer.send(aggregateType,
                    EventUtil.makeMessageForDomainEvent(aggregateType, aggregateId, headers, event,
                            domainEventNameMapping.eventToExternalEventType(aggregateType, event))));

    return Flux.fromStream(messages).flatMap(identity());
  }

  public Flux<Message> publish(Class<?> aggregateType, Object aggregateId, List<DomainEvent> domainEvents) {
    return publish(aggregateType.getName(), aggregateId, domainEvents);
  }
}
