package io.eventuate.tram.micronaut.commands.common;

import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.commands.common.DefaultCommandNameMapping;
import io.micronaut.context.annotation.Requires;

import javax.inject.Singleton;

public class TramCommandCommonFactory {

  @Singleton
  @Requires(missingBeans = CommandNameMapping.class)
  public CommandNameMapping domainEventNameMapping() {
    return new DefaultCommandNameMapping();
  }

}
