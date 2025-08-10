package io.eventuate.tram.micronaut.commands.common;

import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.commands.common.DefaultCommandNameMapping;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

import jakarta.inject.Singleton;

@Factory
public class TramCommandCommonFactory {

  @Singleton
  @Requires(missingBeans = CommandNameMapping.class)
  public CommandNameMapping domainEventNameMapping() {
    return new DefaultCommandNameMapping();
  }

}
