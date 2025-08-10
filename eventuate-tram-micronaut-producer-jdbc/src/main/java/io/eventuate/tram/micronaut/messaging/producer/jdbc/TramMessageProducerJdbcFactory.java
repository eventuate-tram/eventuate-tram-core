package io.eventuate.tram.micronaut.messaging.producer.jdbc;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateCommonJdbcOperations;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.sqldialect.SqlDialectSelector;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import io.eventuate.tram.messaging.producer.jdbc.MessageProducerJdbcImpl;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;

import jakarta.inject.Singleton;

@Factory
public class TramMessageProducerJdbcFactory {

  @Singleton
  public MessageProducerImplementation messageProducerImplementation(EventuateCommonJdbcOperations eventuateCommonJdbcOperations,
                                                                     IdGenerator idGenerator,
                                                                     EventuateSchema eventuateSchema) {
    return new MessageProducerJdbcImpl(eventuateCommonJdbcOperations,
            idGenerator,
            eventuateSchema);
  }
}
