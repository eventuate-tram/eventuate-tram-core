package io.eventuate.tram.consumer.common;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.tram.jdbc.CommonJdbcMessagingConfiguration;
import io.eventuate.tram.messaging.common.sql.SqlDialectConfiguration;
import io.eventuate.tram.messaging.common.sql.SqlDialectSelector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({SqlDialectConfiguration.class, CommonJdbcMessagingConfiguration.class})
public class TramConsumerCommonConfiguration {
  @Bean
  @ConditionalOnMissingBean(DuplicateMessageDetector.class)
  public DuplicateMessageDetector duplicateMessageDetector(EventuateSchema eventuateSchema,
                                                           SqlDialectSelector sqlDialectSelector) {
    return new SqlTableBasedDuplicateMessageDetector(eventuateSchema,
            sqlDialectSelector.getDialect().getCurrentTimeInMillisecondsExpression());
  }
}
