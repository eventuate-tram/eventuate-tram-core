package io.eventuate.tram.spring.reactive.messaging.autoconfigure;

import io.eventuate.tram.spring.messaging.producer.jdbc.reactive.ReactiveTramMessageProducerJdbcConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ReactiveTramMessageProducerJdbcConfiguration.class)
public class ReactiveTramMessageProducerJdbcAutoConfiguration {
}
