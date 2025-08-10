package io.eventuate.tram.micronaut.consumer.jdbc;

import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.EventuateTransactionTemplate;
import io.eventuate.common.jdbc.sqldialect.SqlDialectSelector;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.jdbc.SqlTableBasedDuplicateMessageDetector;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.inject.Singleton;

@Factory
public class TramConsumerJdbcFactory {

  @Singleton
  @Requires(missingProperty = "transactional.noop.duplicate.message.detector.factory.enabled")
  public DuplicateMessageDetector duplicateMessageDetector(@Value("${datasources.default.driver-class-name}") String driver,
                                                           EventuateSchema eventuateSchema,
                                                           SqlDialectSelector sqlDialectSelector,
                                                           EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                                           EventuateTransactionTemplate eventuateTransactionTemplate) {

    return new SqlTableBasedDuplicateMessageDetector(eventuateSchema,
            sqlDialectSelector.getDialect(driver).getCurrentTimeInMillisecondsExpression(),
            eventuateJdbcStatementExecutor,
            eventuateTransactionTemplate);
  }

  @Singleton
  public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
    return new TransactionTemplate(transactionManager);
  }
}
