package io.eventuate.tram.jdbc.micronaut;

import io.eventuate.common.jdbc.EventuateSchema;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;

import javax.inject.Singleton;

@Factory
public class CommonJdbcMessagingFactory
{
  @Singleton
  public EventuateSchema eventuateSchema(@Value("${eventuate.database.schema:}") String eventuateDatabaseSchema) {
    return new EventuateSchema(eventuateDatabaseSchema);
  }
}
