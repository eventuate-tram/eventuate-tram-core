package io.eventuate.tram.micronaut.commands;

import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.micronaut.context.annotation.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.util.Arrays;

@Context
public class CommandDispatcherInitializer {
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Inject
  private CommandDispatcher[] commandDispatchers;

  @PostConstruct
  public void init() {
    logger.info("initializing command dispatchers");
    Arrays.stream(commandDispatchers).forEach(CommandDispatcher::initialize);
    logger.info("initialized command dispatchers");
  }
}
