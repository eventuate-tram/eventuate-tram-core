package io.eventuate.tram.events.publisher;

import io.eventuate.tram.events.common.DomainEvent;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractDomainEventPublisherForAggregateImpl<A, I, E extends DomainEvent> implements DomainEventPublisherForAggregate<A, I, E> {

  private final Class<A> aggregateClass;
  private final Function<A, I> idExtractor;
  private final DomainEventPublisher domainEventPublisher;
  private final Class<E> eventBaseClass;

  protected AbstractDomainEventPublisherForAggregateImpl(Class<A> aggregateClass,
                                                         Function<A, I> idExtractor,
                                                         DomainEventPublisher domainEventPublisher, Class<E> eventBaseClass) {
    this.aggregateClass = aggregateClass;
    this.idExtractor = idExtractor;
    this.domainEventPublisher = domainEventPublisher;
    this.eventBaseClass = eventBaseClass;
  }

  @Override
  public void publishById(I aggregateId, E event) {
    domainEventPublisher.publish(aggregateClass, aggregateId, Collections.singletonList(event));
  }

  @Override
  public void publish(A aggregate, E event) {
    publishById(idExtractor.apply(aggregate), event);
  }

  @Override
  public void publish(A aggregate, List<E> events) {
    Long aggregateId = (Long) idExtractor.apply(aggregate);
    domainEventPublisher.publish(aggregateClass, aggregateId, (List<DomainEvent>) events);
  }

  @Override
  public Class<A> getAggregateClass() {
    return aggregateClass;
  }

  @Override
  public Class<E> getEventBaseClass() {
    return eventBaseClass;
  }
}
