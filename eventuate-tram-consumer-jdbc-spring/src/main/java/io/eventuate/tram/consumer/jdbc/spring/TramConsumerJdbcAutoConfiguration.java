package io.eventuate.tram.consumer.jdbc.spring;

import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.spring.sqldialect.SqlDialectConfiguration;
import io.eventuate.common.jdbc.sqldialect.SqlDialectSelector;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.jdbc.SqlTableBasedDuplicateMessageDetector;
import io.eventuate.tram.jdbc.spring.CommonJdbcMessagingConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@Import({SqlDialectConfiguration.class,
        CommonJdbcMessagingConfiguration.class})
@ConditionalOnMissingBean(DuplicateMessageDetector.class)
public class TramConsumerJdbcAutoConfiguration {

  @Value("${spring.datasource.driver-class-name}")
  private String driver;

  @Bean
  public DuplicateMessageDetector duplicateMessageDetector(EventuateSchema eventuateSchema,
                                                           SqlDialectSelector sqlDialectSelector,
                                                           TransactionTemplate transactionTemplate,
                                                           JdbcTemplate jdbcTemplate) {
    return new SqlTableBasedDuplicateMessageDetector(eventuateSchema,
            sqlDialectSelector.getDialect(driver).getCurrentTimeInMillisecondsExpression(),
            jdbcTemplate,
            transactionTemplate);
  }

}
