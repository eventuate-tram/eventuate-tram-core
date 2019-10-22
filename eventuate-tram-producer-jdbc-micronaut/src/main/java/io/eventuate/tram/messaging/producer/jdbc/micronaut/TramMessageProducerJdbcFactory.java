package io.eventuate.tram.messaging.producer.jdbc.micronaut;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.id.IdGeneratorImpl;
import io.eventuate.common.jdbc.EventuateCommonJdbcOperations;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.sqldialect.SqlDialectSelector;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import io.eventuate.tram.messaging.producer.jdbc.MessageProducerJdbcImpl;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;

import javax.inject.Singleton;

@Factory
public class TramMessageProducerJdbcFactory {

  @Singleton
  public MessageProducerImplementation messageProducerImplementation(@Value("${datasources.default.driver-class-name}") String driver,
                                                                     EventuateCommonJdbcOperations eventuateCommonJdbcOperations,
                                                                     IdGenerator idGenerator,
                                                                     EventuateSchema eventuateSchema,
                                                                     SqlDialectSelector sqlDialectSelector) {
    return new MessageProducerJdbcImpl(eventuateCommonJdbcOperations,
            idGenerator,
            eventuateSchema,
            sqlDialectSelector.getDialect(driver).getCurrentTimeInMillisecondsExpression()
    );
  }

  @Singleton
  public IdGenerator idGenerator() {
    return new IdGeneratorImpl();
  }
}
