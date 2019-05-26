package io.eventuate.tram.messaging.producer.jdbc;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.javaclient.spring.jdbc.IdGenerator;
import io.eventuate.javaclient.spring.jdbc.IdGeneratorImpl;
import io.eventuate.sql.dialect.SqlDialectConfiguration;
import io.eventuate.sql.dialect.SqlDialectSelector;
import io.eventuate.tram.jdbc.CommonJdbcMessagingConfiguration;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import io.eventuate.tram.messaging.producer.common.TramMessagingCommonProducerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({SqlDialectConfiguration.class, CommonJdbcMessagingConfiguration.class, TramMessagingCommonProducerConfiguration.class})
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
  public IdGenerator idGenerator() {
    return new IdGeneratorImpl();
  }
}
