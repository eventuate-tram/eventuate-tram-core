package io.eventuate.tram.spring.reactive.messaging.autoconfigure;

import io.eventuate.tram.spring.messaging.producer.jdbc.reactive.ReactiveTramMessageProducerJdbcConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(ReactiveTramMessageProducerJdbcConfiguration.class)
public class ReactiveTramMessageProducerJdbcAutoConfiguration {
}
