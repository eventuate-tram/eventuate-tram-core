package io.eventuate.tram.spring.reactive.consumer.jdbc;

import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.sqldialect.SqlDialectSelector;
import io.eventuate.common.spring.jdbc.EventuateSchemaConfiguration;
import io.eventuate.common.spring.jdbc.reactive.EventuateSpringReactiveJdbcStatementExecutor;
import io.eventuate.common.spring.jdbc.sqldialect.SqlDialectConfiguration;
import io.eventuate.tram.consumer.common.reactive.ReactiveDuplicateMessageDetector;
import io.eventuate.tram.reactive.consumer.jdbc.ReactiveSqlTableBasedDuplicateMessageDetector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.reactive.TransactionalOperator;

import java.util.Optional;

@AutoConfiguration
@Import({SqlDialectConfiguration.class,
    EventuateSchemaConfiguration.class})
@ConditionalOnMissingBean(ReactiveDuplicateMessageDetector.class)
public class ReactiveTramConsumerJdbcAutoConfiguration {

  @Value("${eventuate.reactive.db.driver}")
  private String driver;

  @Bean
  public ReactiveDuplicateMessageDetector duplicateMessageDetector(EventuateSchema eventuateSchema,
                                                           SqlDialectSelector sqlDialectSelector,
                                                           TransactionalOperator transactionalOperator,
                                                           EventuateSpringReactiveJdbcStatementExecutor eventuateJdbcStatementExecutor) {
    return new ReactiveSqlTableBasedDuplicateMessageDetector(eventuateSchema,
            sqlDialectSelector.getDialect(driver, Optional.empty()).getCurrentTimeInMillisecondsExpression(),
            transactionalOperator,
            eventuateJdbcStatementExecutor);
  }

}
