package io.eventuate.tram.events.publisher;

import io.eventuate.tram.events.common.DomainEvent;

import java.util.Collections;
import java.util.List;

public class ResultWithTypedEvents<Result, Event extends DomainEvent> {
  private final Result result;
  private final List<Event> events;

  public ResultWithTypedEvents(Result result, List<Event> events) {
    this.result = result;
    this.events = events;
  }

  public ResultWithTypedEvents(Result result, Event event) {
    this(result, Collections.singletonList(event));
  }

  public Result getResult() {
    return result;
  }

  public List<Event> getEvents() {
    return events;
  }

}
