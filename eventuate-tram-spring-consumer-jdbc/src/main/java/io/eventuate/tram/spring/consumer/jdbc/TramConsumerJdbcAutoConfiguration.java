package io.eventuate.tram.spring.consumer.jdbc;

import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.EventuateTransactionTemplate;
import io.eventuate.common.spring.jdbc.EventuateCommonJdbcOperationsConfiguration;
import io.eventuate.common.spring.jdbc.EventuateSchemaConfiguration;
import io.eventuate.common.spring.jdbc.sqldialect.SqlDialectConfiguration;
import io.eventuate.common.jdbc.sqldialect.SqlDialectSelector;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.jdbc.SqlTableBasedDuplicateMessageDetector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({SqlDialectConfiguration.class,
    EventuateSchemaConfiguration.class,
    EventuateCommonJdbcOperationsConfiguration.class})
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
