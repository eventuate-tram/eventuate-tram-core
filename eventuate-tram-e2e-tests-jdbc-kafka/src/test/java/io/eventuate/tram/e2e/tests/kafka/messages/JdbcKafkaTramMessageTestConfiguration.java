package io.eventuate.tram.e2e.tests.kafka.messages;

import io.eventuate.jdbckafka.TramJdbcKafkaConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({TramJdbcKafkaConfiguration.class})
public class JdbcKafkaTramMessageTestConfiguration {
}
