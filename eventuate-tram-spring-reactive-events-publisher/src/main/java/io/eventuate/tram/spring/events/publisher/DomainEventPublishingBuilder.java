package io.eventuate.tram.spring.events.publisher;

import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.messaging.common.Message;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

public class DomainEventPublishingBuilder {
  private ReactiveDomainEventPublisher reactiveDomainEventPublisher;
  private LinkedList<EventContainer> eventContainers = new LinkedList<>();

  DomainEventPublishingBuilder(ReactiveDomainEventPublisher reactiveDomainEventPublisher) {
    this.reactiveDomainEventPublisher = reactiveDomainEventPublisher;

    eventContainers.add(new EventContainer());
  }

  AggregateTypeStep aggregateType(String value) {
    eventContainers.peek().aggregateType = value;
    return new AggregateTypeStep();
  }

  AggregateTypeStep aggregateType(Class<?> value) {
    return aggregateType(value.getName());
  }

  public static class EventContainer {
    private EventContainer() {}

    private String aggregateType;
    private Object aggregateId;
    private Map<String, String> headers = Collections.emptyMap();
    private List<DomainEvent> domainEvents = new ArrayList<>();

    String getAggregateType() {
      return aggregateType;
    }

    Object getAggregateId() {
      return aggregateId;
    }

    Map<String, String> getHeaders() {
      return headers;
    }

    List<DomainEvent> getDomainEvents() {
      return domainEvents;
    }
  }



  public class AggregateTypeStep {
    private AggregateTypeStep() {}

    public AggregateIdStep aggregateId(Object value) {
      eventContainers.peek().aggregateId = value;
      return new AggregateIdStep();
    }
  }



  public class AggregateIdStep {
    private AggregateIdStep() {}

    public EventHeadersStep headers(Map<String, String> value) {
      eventContainers.peek().headers = value;
      return new EventHeadersStep();
    }

    public EventHeadersStep event(DomainEvent value) {
      return events(singletonList(value));
    }

    public EventHeadersStep events(List<DomainEvent> value) {
      eventContainers.peek().domainEvents.addAll(value);
      return new EventHeadersStep();
    }
  }



  public class EventHeadersStep {
    private EventHeadersStep() {}

    public EventHeadersStep event(DomainEvent value) {
      return events(singletonList(value));
    }

    public EventHeadersStep events(List<DomainEvent> value) {
      eventContainers.peek().domainEvents.addAll(value);
      return this;
    }

    public AggregateTypeStep aggregateType(String value) {
      return next().aggregateType(value);
    }

    public AggregateTypeStep aggregateType(Class<?> value) {
      return next().aggregateType(value);
    }

    public Mono<List<Message>> publish() {
      return reactiveDomainEventPublisher.publish(eventContainers);
    }

    private DomainEventPublishingBuilder next() {
      DomainEventPublishingBuilder domainEventPublishingBuilder = DomainEventPublishingBuilder.this;

      domainEventPublishingBuilder.eventContainers.addFirst(new EventContainer());

      return domainEventPublishingBuilder;
    }
  }

}
