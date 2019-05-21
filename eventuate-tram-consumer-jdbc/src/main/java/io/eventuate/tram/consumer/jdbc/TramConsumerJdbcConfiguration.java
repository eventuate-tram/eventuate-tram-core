package io.eventuate.tram.consumer.jdbc;

import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.sql.dialect.SqlDialectConfiguration;
import io.eventuate.sql.dialect.SqlDialectSelector;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.jdbc.CommonJdbcMessagingConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@Import({SqlDialectConfiguration.class, CommonJdbcMessagingConfiguration.class})
public class TramConsumerJdbcConfiguration {

  @Value("${spring.datasource.driver-class-name}")
  private String driver;

  @Bean
  @ConditionalOnMissingBean(DuplicateMessageDetector.class)
  public DuplicateMessageDetector duplicateMessageDetector(EventuateSchema eventuateSchema,
                                                           SqlDialectSelector sqlDialectSelector, TransactionTemplate transactionTemplate) {
    return new SqlTableBasedDuplicateMessageDetector(eventuateSchema,
            sqlDialectSelector.getDialect(driver).getCurrentTimeInMillisecondsExpression(), transactionTemplate);
  }

}
