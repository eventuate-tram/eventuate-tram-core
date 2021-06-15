package io.eventuate.tram.reactive.events.subscriber;

import io.eventuate.tram.messaging.common.Message;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class ReactiveDomainEventHandlers {
  private List<ReactiveDomainEventHandler> handlers;

  public ReactiveDomainEventHandlers(List<ReactiveDomainEventHandler> handlers) {
    this.handlers = handlers;
  }

  public Set<String> getAggregateTypesAndEvents() {
    return handlers.stream().map(ReactiveDomainEventHandler::getAggregateType).collect(toSet());
  }

  public List<ReactiveDomainEventHandler> getHandlers() {
    return handlers;
  }

  public Optional<ReactiveDomainEventHandler> findTargetMethod(Message message) {
    return handlers.stream().filter(h -> h.handles(message)).findFirst();
  }
}
