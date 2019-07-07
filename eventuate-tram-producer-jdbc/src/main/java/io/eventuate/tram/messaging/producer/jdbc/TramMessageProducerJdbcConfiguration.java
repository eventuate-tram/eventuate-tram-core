package io.eventuate.tram.messaging.producer.jdbc;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.id.IdGeneratorImpl;
import io.eventuate.common.jdbc.EventuateCommonJdbcOperations;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.EventuateSchemaConfiguration;
import io.eventuate.common.jdbc.sqldialect.SqlDialectConfiguration;
import io.eventuate.common.jdbc.sqldialect.SqlDialectSelector;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import io.eventuate.tram.messaging.producer.common.TramMessagingCommonProducerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Import({SqlDialectConfiguration.class, EventuateSchemaConfiguration.class, TramMessagingCommonProducerConfiguration.class})
public class TramMessageProducerJdbcConfiguration {

  @Value("${spring.datasource.driver-class-name}")
  private String driver;

  @Bean
  public MessageProducerImplementation messageProducerImplementation(EventuateSchema eventuateSchema,
                                                       SqlDialectSelector sqlDialectSelector) {
    return new MessageProducerJdbcImpl(eventuateSchema,
            sqlDialectSelector.getDialect(driver).getCurrentTimeInMillisecondsExpression()
    );
  }

  @Bean
  public EventuateCommonJdbcOperations eventuateCommonJdbcOperations(JdbcTemplate jdbcTemplate) {
    return new EventuateCommonJdbcOperations(jdbcTemplate);
  }

  @Bean
  public IdGenerator idGenerator() {
    return new IdGeneratorImpl();
  }
}
