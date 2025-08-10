package io.eventuate.tram.micronaut.events.subscriber;

import io.eventuate.tram.events.subscriber.DomainEventDispatcher;
import io.micronaut.context.annotation.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.util.Arrays;

@Context
public class DomainEventDispatcherInitializer {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private DomainEventDispatcher[] domainEventDispatchers;

  public DomainEventDispatcherInitializer(DomainEventDispatcher[] domainEventDispatchers) {
    this.domainEventDispatchers = domainEventDispatchers;
  }

  @PostConstruct
  public void init() {
    logger.info("Initializing domain event dispatchers");
    Arrays.stream(domainEventDispatchers).forEach(DomainEventDispatcher::initialize);
    logger.info("Initialized domain event dispatchers");
  }
}
