package io.eventuate.tram.micronaut.events.common;

import io.eventuate.tram.events.common.DefaultDomainEventNameMapping;
import io.eventuate.tram.events.common.DomainEventNameMapping;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

import jakarta.inject.Singleton;

@Factory
public class TramEventsCommonFactory {

  @Singleton
  @Requires(missingBeans = DomainEventNameMapping.class)
  public DomainEventNameMapping domainEventNameMapping() {
    return new DefaultDomainEventNameMapping();
  }

}
