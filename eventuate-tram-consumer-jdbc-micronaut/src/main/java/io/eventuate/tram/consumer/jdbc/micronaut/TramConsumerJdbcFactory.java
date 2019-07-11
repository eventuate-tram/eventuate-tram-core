package io.eventuate.tram.consumer.jdbc.micronaut;

import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.micronaut.EventuateMicronautTransactionManagement;
import io.eventuate.common.jdbc.sqldialect.SqlDialectSelector;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;

import javax.inject.Singleton;

@Factory
public class TramConsumerJdbcFactory {

  @Value("${datasources.default.driverclassname}")
  private String driver;

  @Singleton
  @Requires(missingProperty = "transactional.noop.duplicate.message.detector.factory.enabled")
  public DuplicateMessageDetector duplicateMessageDetector(EventuateSchema eventuateSchema,
                                                           SqlDialectSelector sqlDialectSelector,
                                                           EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                                           EventuateMicronautTransactionManagement micronautTransactionManagement) {
    return new EventuateMicronautSqlTableBasedDuplicateMessageDetector(eventuateSchema,
            sqlDialectSelector.getDialect(driver).getCurrentTimeInMillisecondsExpression(),
            micronautTransactionManagement,
            eventuateJdbcStatementExecutor);
  }

}
