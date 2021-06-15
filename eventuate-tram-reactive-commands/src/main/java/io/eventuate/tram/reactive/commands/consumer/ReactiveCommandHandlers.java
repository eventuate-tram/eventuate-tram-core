package io.eventuate.tram.reactive.commands.consumer;

import io.eventuate.tram.messaging.common.Message;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class ReactiveCommandHandlers {
  private List<ReactiveCommandHandler> handlers;

  public ReactiveCommandHandlers(List<ReactiveCommandHandler> handlers) {
    this.handlers = handlers;
  }

  public Set<String> getChannels() {
    return handlers.stream().map(ReactiveCommandHandler::getChannel).collect(toSet());
  }

  public Optional<ReactiveCommandHandler> findTargetMethod(Message message) {
    return handlers.stream().filter(h -> h.handles(message)).findFirst();
  }

  public Optional<ReactiveCommandExceptionHandler> findExceptionHandler(ReactiveCommandHandler reactiveCommandHandler, Throwable cause) {
    throw new UnsupportedOperationException(String.format("A command handler for command of type %s on channel %s threw an exception",
            reactiveCommandHandler.getCommandClass().getName(),
            reactiveCommandHandler.getChannel()),
            cause);
  }

  public List<ReactiveCommandHandler> getHandlers() {
    return handlers;
  }
}
