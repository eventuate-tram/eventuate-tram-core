package io.eventuate.tram.spring.messaging.autoconfigure;

import io.eventuate.tram.spring.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(TramMessageProducerJdbcConfiguration.class)
@Import(TramMessageProducerJdbcConfiguration.class)
public class TramMessageProducerJdbcAutoConfiguration {
}
