package io.eventuate.tram.events.publisher;


import io.eventuate.tram.events.common.DomainEvent;

import java.util.Arrays;
import java.util.List;

public class ResultWithEvents<T> {

  public final T result;
  public final List<DomainEvent> events;

  public ResultWithEvents(T result, List<DomainEvent> events) {
    this.result = result;
    this.events = events;
  }

  public ResultWithEvents(T result, DomainEvent... events) {
    this.result = result;
    this.events = Arrays.asList(events);
  }
}
