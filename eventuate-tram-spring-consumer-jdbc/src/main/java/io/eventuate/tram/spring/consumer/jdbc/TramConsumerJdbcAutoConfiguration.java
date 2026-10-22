package io.eventuate.tram.spring.consumer.jdbc;

import io.eventuate.common.database.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.database.jdbc.EventuateSchema;
import io.eventuate.common.database.jdbc.EventuateTransactionTemplate;
import io.eventuate.common.database.jdbc.sqldialect.SqlDialectSelector;
import io.eventuate.common.database.spring.jdbc.EventuateCommonJdbcOperationsConfiguration;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.jdbc.SqlTableBasedDuplicateMessageDetector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(EventuateCommonJdbcOperationsConfiguration.class)
@ConditionalOnMissingBean(DuplicateMessageDetector.class)
public class TramConsumerJdbcAutoConfiguration {

  @Value("${spring.datasource.driver-class-name}")
  private String driver;

  @Bean
  public DuplicateMessageDetector duplicateMessageDetector(EventuateSchema eventuateSchema,
                                                           SqlDialectSelector sqlDialectSelector,
                                                           EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                                           EventuateTransactionTemplate eventuateTransactionTemplate) {
    return new SqlTableBasedDuplicateMessageDetector(eventuateSchema,
            sqlDialectSelector.getDialect(driver).getCurrentTimeInMillisecondsExpression(),
            eventuateJdbcStatementExecutor,
            eventuateTransactionTemplate);
  }

}
