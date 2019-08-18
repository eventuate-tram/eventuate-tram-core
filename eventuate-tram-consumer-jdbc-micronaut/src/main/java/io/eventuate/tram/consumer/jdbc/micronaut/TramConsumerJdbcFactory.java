package io.eventuate.tram.consumer.jdbc.micronaut;

import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.sqldialect.SqlDialectSelector;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.jdbc.SqlTableBasedDuplicateMessageDetector;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Singleton;
import javax.sql.DataSource;

@Factory
public class TramConsumerJdbcFactory {

  @Value("${datasources.default.driver-class-name}")
  private String driver;

  @Singleton
  @Requires(missingProperty = "transactional.noop.duplicate.message.detector.factory.enabled")
  public DuplicateMessageDetector duplicateMessageDetector(EventuateSchema eventuateSchema,
                                                           SqlDialectSelector sqlDialectSelector,
                                                           JdbcTemplate jdbcTemplate,
                                                           TransactionTemplate transactionTemplate) {

    return new SqlTableBasedDuplicateMessageDetector(eventuateSchema,
            sqlDialectSelector.getDialect(driver).getCurrentTimeInMillisecondsExpression(),
            jdbcTemplate,
            transactionTemplate);
  }

  @Singleton
  @Requires(missingBeans = TransactionTemplate.class)
  public TransactionTemplate transactionTemplate(DataSource dataSource) {
    return new TransactionTemplate(new DataSourceTransactionManager(dataSource));
  }
}
