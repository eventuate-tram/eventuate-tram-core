package io.eventuate.tram.commands.micronaut;

import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.micronaut.context.annotation.Context;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Arrays;

@Context
public class CommandDispatcherInitializer {

  @Inject
  private CommandDispatcher[] commandDispatchers;

  @PostConstruct
  public void init() {
    Arrays.stream(commandDispatchers).forEach(CommandDispatcher::initialize);
  }
}
