package io.eventuate.tram.spring.messaging.autoconfigure;

import io.eventuate.tram.spring.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnClass(TramMessageProducerJdbcConfiguration.class)
@Import(TramMessageProducerJdbcConfiguration.class)
public class TramMessageProducerJdbcAutoConfiguration {
}
